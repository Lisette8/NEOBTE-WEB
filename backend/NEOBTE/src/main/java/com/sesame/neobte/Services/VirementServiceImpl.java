package com.sesame.neobte.Services;

import com.sesame.neobte.Config.AccountTypePolicy;
import com.sesame.neobte.DTO.Requests.Virement.InternalTransferCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementHistoryFilterDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.TransferConstraintsDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementHistoryPageDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Entities.Class.*;

import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.*;
import org.springframework.transaction.annotation.Transactional;
import com.sesame.neobte.Security.Services.Fraude.FraudeService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@Service
@Slf4j
public class VirementServiceImpl implements VirementService {

    private final IVirementRepository virementRepository;
    private final ICompteRepository compteRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final ICompteInterneRepository compteInterneRepository;
    private final IFraisTransactionRepository fraisTransactionRepository;
    private final FraudeService fraudeService;
    private final NotificationService notificationService;

    @Value("${neobte.fee-account.name:NEOBTE_FEES}")
    private String feeAccountName;

    public VirementServiceImpl(
            IVirementRepository virementRepository,
            ICompteRepository compteRepository,
            IUtilisateurRepository utilisateurRepository,
            ICompteInterneRepository compteInterneRepository,
            IFraisTransactionRepository fraisTransactionRepository,
            FraudeService fraudeService,
            NotificationService notificationService) {
        this.virementRepository = virementRepository;
        this.compteRepository = compteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.compteInterneRepository = compteInterneRepository;
        this.fraisTransactionRepository = fraisTransactionRepository;
        this.fraudeService = fraudeService;
        this.notificationService = notificationService;
    }

    @Override
    public RecipientPreviewDTO resolveRecipient(String identifier, Long senderUserId) {
        FraudeConfig cfg = fraudeService.getConfigEntity();
        Compte senderPrimary = getPrimaryAccount(senderUserId);
        TypeCompte senderType = senderPrimary != null ? senderPrimary.getTypeCompte() : TypeCompte.COURANT;

        double feeRate    = AccountTypePolicy.feeRate(senderType, cfg);
        double maxXfer    = AccountTypePolicy.maxTransferAmount(senderType, cfg);
        double dailyAmt   = AccountTypePolicy.dailyAmountLimit(senderType, cfg);
        int    dailyCnt   = AccountTypePolicy.dailyCountLimit(senderType, cfg);
        int    monthlyCnt = AccountTypePolicy.monthlyCountLimit(senderType, cfg);
        boolean canSend   = AccountTypePolicy.canSendExternal(senderType);

        Utilisateur recipient = findByIdentifier(identifier);
        if (recipient == null) return notFoundPreview(feeRate, maxXfer, dailyAmt, dailyCnt, monthlyCnt, canSend);

        Compte primary = getPrimaryAccount(recipient.getIdUtilisateur());
        if (primary == null) return notFoundPreview(feeRate, maxXfer, dailyAmt, dailyCnt, monthlyCnt, canSend);

        return new RecipientPreviewDTO(
                recipient.getPrenom() + " " + recipient.getNom(),
                maskIdentifier(identifier),
                primary.getIdCompte(),
                primary.getTypeCompte().name(),
                true,
                recipient.getPhotoUrl(),
                feeRate, null, maxXfer, dailyAmt, dailyCnt, monthlyCnt, canSend);
    }

    @Override
    public TransferConstraintsDTO getConstraints(Long senderUserId, boolean internal) {
        FraudeConfig cfg = fraudeService.getConfigEntity();
        Compte senderPrimary = getPrimaryAccount(senderUserId);
        TypeCompte type = senderPrimary != null ? senderPrimary.getTypeCompte() : TypeCompte.COURANT;

        double feeRate    = internal ? 0.0 : AccountTypePolicy.feeRate(type, cfg);
        double maxXfer    = AccountTypePolicy.maxTransferAmount(type, cfg);
        double dailyAmt   = AccountTypePolicy.dailyAmountLimit(type, cfg);
        int    dailyCnt   = AccountTypePolicy.dailyCountLimit(type, cfg);
        int    monthlyCnt = AccountTypePolicy.monthlyCountLimit(type, cfg);
        boolean canSend   = AccountTypePolicy.canSendExternal(type);

        return new TransferConstraintsDTO(
                feeRate, maxXfer, dailyAmt, dailyCnt, monthlyCnt, canSend,
                AccountTypePolicy.purpose(type), AccountTypePolicy.label(type));
    }

    @Override
    @Transactional
    public VirementResponseDTO effectuerVirement(VirementCreateDTO dto, Long senderUserId) {
        if (dto.getMontant() <= 0) throw new BadRequestException("Le montant doit être supérieur à 0");

        Optional<Virement> existing = virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) return mapToResponseDTO(existing.get());

        Utilisateur sender = utilisateurRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur introuvable"));

        fraudeService.enforceHardLimits(senderUserId, dto.getMontant());

        Compte compteSource = getPrimaryAccount(senderUserId);
        if (compteSource == null)
            throw new BadRequestException(
                    "Vous n'avez pas de compte bancaire actif. Veuillez en ouvrir un d'abord.");

        TypeCompte sourceType = compteSource.getTypeCompte();
        FraudeConfig cfg = fraudeService.getConfigEntity();

        AccountTypePolicy.assertCanSendExternal(sourceType);
        AccountTypePolicy.assertMaxTransfer(sourceType, dto.getMontant(), cfg);

        Date dayStart   = dayStartDate();
        Date monthStart = monthStartDate();
        long countToday     = virementRepository.countOutgoingFromCompteSince(compteSource.getIdCompte(), dayStart);
        Double sentToday    = virementRepository.sumOutgoingFromCompteSince(compteSource.getIdCompte(), dayStart);
        long countThisMonth = virementRepository.countOutgoingFromCompteSince(compteSource.getIdCompte(), monthStart);

        AccountTypePolicy.assertDailyCount(sourceType, countToday, cfg);
        AccountTypePolicy.assertDailyAmount(sourceType, sentToday != null ? sentToday : 0.0, dto.getMontant(), cfg);
        AccountTypePolicy.assertMonthlyCount(sourceType, countThisMonth, cfg);

        Utilisateur recipient = findByIdentifier(dto.getRecipientIdentifier());
        if (recipient == null)
            throw new ResourceNotFoundException("Aucun utilisateur trouvé avec cet email ou ce numéro de téléphone");
        if (recipient.getIdUtilisateur().equals(senderUserId))
            throw new BadRequestException("Vous ne pouvez pas vous transférer de l'argent à vous-même");

        Compte compteDestination = getPrimaryAccount(recipient.getIdUtilisateur());
        if (compteDestination == null)
            throw new BadRequestException("Le bénéficiaire n'a pas de compte bancaire actif");

        double feeRate     = AccountTypePolicy.feeRate(sourceType, cfg);
        double frais       = Math.round(dto.getMontant() * feeRate * 1000.0) / 1000.0;
        double totalDebite = dto.getMontant() + frais;

        Long lowId  = Math.min(compteSource.getIdCompte(), compteDestination.getIdCompte());
        Long highId = Math.max(compteSource.getIdCompte(), compteDestination.getIdCompte());
        Compte lockedLow  = compteRepository.findByIdForUpdate(lowId).orElseThrow();
        Compte lockedHigh = compteRepository.findByIdForUpdate(highId).orElseThrow();
        Compte lockedSource = lockedLow.getIdCompte().equals(compteSource.getIdCompte())      ? lockedLow : lockedHigh;
        Compte lockedDest   = lockedLow.getIdCompte().equals(compteDestination.getIdCompte()) ? lockedLow : lockedHigh;

        if (lockedSource.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Votre compte n'est pas actif");
        if (lockedSource.getSolde() < totalDebite)
            throw new BadRequestException(String.format(
                    "Solde insuffisant. Montant : %.3f TND + frais : %.3f TND = %.3f TND requis. " +
                            "Solde actuel : %.3f TND.",
                    dto.getMontant(), frais, totalDebite, lockedSource.getSolde()));

        lockedSource.setSolde(lockedSource.getSolde() - totalDebite);
        lockedDest.setSolde(lockedDest.getSolde() + dto.getMontant());
        compteRepository.save(lockedSource);
        compteRepository.save(lockedDest);

        Virement saved = saveVirement(lockedSource, lockedDest, dto.getMontant(), frais, feeRate, dto.getIdempotencyKey());
        creditFeeAccount(frais);
        auditFee(saved, frais, feeRate);
        scheduleAsyncFraudCheck(saved.getIdVirement(), senderUserId);

        notificationService.notifyUser(senderUserId, NotificationType.TRANSFER_SENT,
                "Virement envoyé",
                String.format("Vous avez envoyé %.3f TND à %s %s. Frais : %.3f TND.",
                        dto.getMontant(), recipient.getPrenom(), recipient.getNom(), frais),
                "/virement-view");
        notificationService.notifyUser(recipient.getIdUtilisateur(), NotificationType.TRANSFER_RECEIVED,
                "Virement reçu",
                String.format("Vous avez reçu %.3f TND de %s %s.",
                        dto.getMontant(), sender.getPrenom(), sender.getNom()),
                "/virement-view");

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public VirementResponseDTO effectuerVirementInterne(InternalTransferCreateDTO dto, Long senderUserId) {
        if (dto.getMontant() == null || dto.getMontant() <= 0)
            throw new BadRequestException("Le montant doit être supérieur à 0");
        if (dto.getCompteSourceId() == null || dto.getCompteDestinationId() == null)
            throw new BadRequestException("Comptes source/destination invalides");
        if (dto.getCompteSourceId().equals(dto.getCompteDestinationId()))
            throw new BadRequestException("Veuillez sélectionner deux comptes différents");
        if (dto.getIdempotencyKey() == null || dto.getIdempotencyKey().isBlank())
            throw new BadRequestException("Clé d'idempotence invalide");

        Optional<Virement> existing = virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) return mapToResponseDTO(existing.get());

        fraudeService.enforceHardLimits(senderUserId, dto.getMontant());

        FraudeConfig cfg = fraudeService.getConfigEntity();

        Long lowId  = Math.min(dto.getCompteSourceId(), dto.getCompteDestinationId());
        Long highId = Math.max(dto.getCompteSourceId(), dto.getCompteDestinationId());
        Compte lockedLow  = compteRepository.findByIdForUpdate(lowId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
        Compte lockedHigh = compteRepository.findByIdForUpdate(highId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));

        Compte lockedSource = lockedLow.getIdCompte().equals(dto.getCompteSourceId()) ? lockedLow : lockedHigh;
        Compte lockedDest   = lockedLow.getIdCompte().equals(dto.getCompteDestinationId()) ? lockedLow : lockedHigh;

        if (!Objects.equals(lockedSource.getUtilisateur().getIdUtilisateur(), senderUserId))
            throw new BadRequestException("Compte source invalide");
        if (!Objects.equals(lockedDest.getUtilisateur().getIdUtilisateur(), senderUserId))
            throw new BadRequestException("Compte destination invalide");
        if (lockedSource.getStatutCompte() != StatutCompte.ACTIVE || lockedDest.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Vos deux comptes doivent être actifs pour effectuer un transfert interne");

        TypeCompte sourceType = lockedSource.getTypeCompte();
        AccountTypePolicy.assertMaxTransfer(sourceType, dto.getMontant(), cfg);

        Date monthStart = monthStartDate();
        long countThisMonth = virementRepository.countOutgoingFromCompteSince(lockedSource.getIdCompte(), monthStart);
        AccountTypePolicy.assertMonthlyCount(sourceType, countThisMonth, cfg);

        if (lockedSource.getSolde() < dto.getMontant())
            throw new BadRequestException(String.format(
                    "Solde insuffisant sur le compte source. Requis : %.3f TND. Disponible : %.3f TND.",
                    dto.getMontant(), lockedSource.getSolde()));

        lockedSource.setSolde(lockedSource.getSolde() - dto.getMontant());
        lockedDest.setSolde(lockedDest.getSolde() + dto.getMontant());
        compteRepository.save(lockedSource);
        compteRepository.save(lockedDest);

        Virement saved = saveVirement(lockedSource, lockedDest, dto.getMontant(), 0.0, 0.0, dto.getIdempotencyKey());
        scheduleAsyncFraudCheck(saved.getIdVirement(), senderUserId);

        notificationService.notifyUser(senderUserId, NotificationType.TRANSFER_SENT,
                "Transfert interne effectué",
                String.format("%.3f TND transférés du compte #%d vers le compte #%d.",
                        dto.getMontant(), lockedSource.getIdCompte(), lockedDest.getIdCompte()),
                "/virement-view");

        return mapToResponseDTO(saved);
    }

    @Override
    public List<VirementResponseDTO> getVirementsUtilisateur(Long userId) {
        List<Long> compteIds = compteRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream().map(Compte::getIdCompte).toList();
        return virementRepository.findAll().stream()
                .filter(v -> compteIds.contains(v.getCompteDe().getIdCompte())
                        || compteIds.contains(v.getCompteA().getIdCompte()))
                .sorted(Comparator.comparing(Virement::getDateDeVirement).reversed())
                .map(this::mapToResponseDTO).toList();
    }

    @Override
    public VirementHistoryPageDTO getFilteredHistory(Long userId, VirementHistoryFilterDTO filter) {
        List<Long> myCompteIds = compteRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream().map(Compte::getIdCompte).toList();

        // ── Date cutoff ───────────────────────────────────────────────────
        java.util.Date cutoff = null;
        if (filter.getPeriod() != null) {
            long nowMs = System.currentTimeMillis();
            cutoff = switch (filter.getPeriod()) {
                case "today" -> new java.util.Date(nowMs - 86_400_000L);
                case "7d"    -> new java.util.Date(nowMs - 7L  * 86_400_000L);
                case "30d"   -> new java.util.Date(nowMs - 30L * 86_400_000L);
                case "3m"    -> new java.util.Date(nowMs - 90L * 86_400_000L);
                default      -> null; // "all"
            };
        }
        final java.util.Date finalCutoff = cutoff;

        // ── Search normalise ──────────────────────────────────────────────
        final String q = (filter.getSearch() != null && !filter.getSearch().isBlank())
                ? filter.getSearch().trim().toLowerCase()
                : null;

        // ── Stream + filter ───────────────────────────────────────────────
        List<Virement> all = virementRepository.findAll().stream()
                .filter(v -> myCompteIds.contains(v.getCompteDe().getIdCompte())
                        || myCompteIds.contains(v.getCompteA().getIdCompte()))
                .filter(v -> finalCutoff == null || !v.getDateDeVirement().before(finalCutoff))
                .filter(v -> {
                    if (q == null) return true;
                    VirementResponseDTO dto = mapToResponseDTO(v);
                    return (dto.getRecipientName() != null && dto.getRecipientName().toLowerCase().contains(q))
                            || (dto.getSenderName()    != null && dto.getSenderName().toLowerCase().contains(q))
                            || String.valueOf(v.getIdVirement()).contains(q)
                            || (v.getMontant() != null && String.format("%.3f", v.getMontant()).contains(q));
                })
                .filter(v -> {
                    if (filter.getType() == null || filter.getType().equals("all")) return true;
                    boolean isMineSource = myCompteIds.contains(v.getCompteDe().getIdCompte());
                    boolean isMineDestination = myCompteIds.contains(v.getCompteA().getIdCompte());
                    boolean isInternal = isMineSource && isMineDestination;
                    return switch (filter.getType()) {
                        case "internal" -> isInternal;
                        case "sent"     -> isMineSource && !isInternal;
                        case "received" -> isMineDestination && !isInternal;
                        default -> true;
                    };
                })
                .toList();

        // ── Sort ─────────────────────────────────────────────────────────
        String sort = filter.getSort() != null ? filter.getSort() : "date-desc";
        List<Virement> sorted = new java.util.ArrayList<>(all);
        sorted.sort(switch (sort) {
            case "date-asc"    -> Comparator.comparing(Virement::getDateDeVirement);
            case "amount-desc" -> Comparator.comparing(Virement::getMontant).reversed();
            case "amount-asc"  -> Comparator.comparing(Virement::getMontant);
            default            -> Comparator.comparing(Virement::getDateDeVirement).reversed();
        });

        // ── Summary totals (on full result before pagination) ─────────────
        double totalSent = 0, totalReceived = 0;
        for (Virement v : sorted) {
            boolean isMineSource = myCompteIds.contains(v.getCompteDe().getIdCompte());
            boolean isMineDestination = myCompteIds.contains(v.getCompteA().getIdCompte());
            boolean isInternal = isMineSource && isMineDestination;
            if (isInternal) continue;
            double frais = v.getFrais() != null ? v.getFrais() : 0.0;
            if (isMineSource) totalSent += v.getMontant() + frais;
            else if (isMineDestination) totalReceived += v.getMontant();
        }

        // ── Pagination ────────────────────────────────────────────────────
        int pageNum  = Math.max(0, filter.getPage());
        int pageSize = Math.min(100, Math.max(1, filter.getSize()));
        int total    = sorted.size();
        int totalPgs = (int) Math.ceil((double) total / pageSize);
        int from     = Math.min(pageNum * pageSize, total);
        int to       = Math.min(from + pageSize, total);

        List<VirementResponseDTO> page = sorted.subList(from, to).stream()
                .map(this::mapToResponseDTO).toList();

        return VirementHistoryPageDTO.builder()
                .content(page)
                .page(pageNum)
                .size(pageSize)
                .totalElements(total)
                .totalPages(totalPgs)
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .build();
    }

    @Override
    public List<VirementResponseDTO> getAllVirements() {
        return virementRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public VirementResponseDTO getVirementById(Long virementId) {
        return mapToResponseDTO(virementRepository.findById(virementId)
                .orElseThrow(() -> new ResourceNotFoundException("Virement introuvable")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Compte getPrimaryAccount(Long userId) {
        for (TypeCompte type : List.of(TypeCompte.COURANT, TypeCompte.PROFESSIONNEL, TypeCompte.EPARGNE)) {
            List<Compte> list = compteRepository
                    .findByUtilisateur_IdUtilisateurAndTypeCompteAndStatutCompteOrderByDateCreationAsc(
                            userId, type, StatutCompte.ACTIVE);
            if (!list.isEmpty()) return list.get(0);
        }
        List<Compte> any = compteRepository
                .findByUtilisateur_IdUtilisateurAndStatutCompteOrderByDateCreationAsc(userId, StatutCompte.ACTIVE);
        return any.isEmpty() ? null : any.get(0);
    }

    private Utilisateur findByIdentifier(String identifier) {
        if (identifier == null) return null;
        String t = identifier.trim();
        Utilisateur u = utilisateurRepository.findByEmail(t);
        return u != null ? u : utilisateurRepository.findByTelephone(t);
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null) return null;
        if (identifier.contains("@")) {
            int at = identifier.indexOf('@');
            String local = identifier.substring(0, at);
            return (local.length() <= 2 ? local : local.substring(0, 2)) + "***" + identifier.substring(at);
        }
        return identifier.length() <= 4 ? "****"
                : "*".repeat(identifier.length() - 4) + identifier.substring(identifier.length() - 4);
    }

    private Date dayStartDate() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date monthStartDate() {
        return Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Virement saveVirement(Compte src, Compte dst, double montant,
                                  double frais, double taux, String key) {
        Virement v = new Virement();
        v.setCompteDe(src); v.setCompteA(dst);
        v.setMontant(montant); v.setFrais(frais); v.setTauxFrais(taux);
        v.setDateDeVirement(new Date()); v.setIdempotencyKey(key);
        return virementRepository.save(v);
    }

    private void creditFeeAccount(double frais) {
        if (frais <= 0) return;
        CompteInterne fee = compteInterneRepository.findByNom(feeAccountName)
                .orElseThrow(() -> new IllegalStateException("Compte interne des frais introuvable."));
        fee.setSolde(fee.getSolde() + frais);
        compteInterneRepository.save(fee);
    }

    private void auditFee(Virement saved, double frais, double taux) {
        if (frais <= 0) return;
        FraisTransaction ft = new FraisTransaction();
        ft.setVirement(saved); ft.setMontantFrais(frais); ft.setTauxApplique(taux);
        fraisTransactionRepository.save(ft);
    }

    private void scheduleAsyncFraudCheck(Long savedId, Long senderId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { fraudeService.analyzeTransferAsync(savedId, senderId); }
        });
    }

    private VirementResponseDTO mapToResponseDTO(Virement v) {
        Utilisateur sender    = v.getCompteDe().getUtilisateur();
        Utilisateur recipient = v.getCompteA().getUtilisateur();
        double frais = v.getFrais() != null ? v.getFrais() : 0.0;
        return new VirementResponseDTO(
                v.getIdVirement(),
                v.getCompteDe().getIdCompte(), v.getCompteA().getIdCompte(),
                recipient != null ? recipient.getPrenom() + " " + recipient.getNom() : "—",
                sender    != null ? sender.getPrenom()    + " " + sender.getNom()    : "—",
                v.getMontant(), frais, v.getMontant() + frais, v.getTauxFrais(),
                v.getDateDeVirement(), v.getIdempotencyKey());
    }

    private RecipientPreviewDTO notFoundPreview(double feeRate, double maxXfer, double dailyAmt,
                                                int dailyCnt, int monthlyCnt, boolean canSend) {
        return new RecipientPreviewDTO(null, null, null, null, false, null,
                feeRate, null, maxXfer, dailyAmt, dailyCnt, monthlyCnt, canSend);
    }
}