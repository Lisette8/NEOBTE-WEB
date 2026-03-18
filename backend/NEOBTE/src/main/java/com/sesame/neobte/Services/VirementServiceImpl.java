package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.InternalTransferCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.TransferConstraintsDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Entities.Class.*;

import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.*;
import com.sesame.neobte.Services.Other.AdminEventPublisher;
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
    private final AdminEventPublisher adminEventPublisher;

    @Value("${neobte.transfer.fee-rate:0.005}")
    private double feeRate;

    @Value("${neobte.fee-account.name:NEOBTE_FEES}")
    private String feeAccountName;

    public VirementServiceImpl(
            IVirementRepository virementRepository,
            ICompteRepository compteRepository,
            IUtilisateurRepository utilisateurRepository,
            ICompteInterneRepository compteInterneRepository,
            IFraisTransactionRepository fraisTransactionRepository,
            FraudeService fraudeService,
            NotificationService notificationService,
            AdminEventPublisher adminEventPublisher) {
        this.virementRepository = virementRepository;
        this.compteRepository = compteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.compteInterneRepository = compteInterneRepository;
        this.fraisTransactionRepository = fraisTransactionRepository;
        this.fraudeService = fraudeService;
        this.notificationService = notificationService;
        this.adminEventPublisher = adminEventPublisher;
    }

    private static final int PREMIUM_DAILY_LIMIT = 50;

    @Override
    public RecipientPreviewDTO resolveRecipient(String identifier, Long senderUserId) {
        FraudeConfig cfg = fraudeService.getConfigEntity();
        int effectiveDailyCountLimit = cfg.getDailyCountLimit();
        if (senderUserId != null) {
            Utilisateur sender = utilisateurRepository.findById(senderUserId).orElse(null);
            if (sender != null && sender.isPremium()) {
                effectiveDailyCountLimit = PREMIUM_DAILY_LIMIT;
            }
        }

        Utilisateur recipient = findByIdentifier(identifier);
        if (recipient == null) return new RecipientPreviewDTO(
                null, null, null, null, false, null, feeRate, null,
                cfg.getLargeTransferThreshold(), cfg.getDailyAmountLimit(), effectiveDailyCountLimit);

        Compte primary = getPrimaryAccount(recipient.getIdUtilisateur());
        if (primary == null) return new RecipientPreviewDTO(
                null, null, null, null, false, null, feeRate, null,
                cfg.getLargeTransferThreshold(), cfg.getDailyAmountLimit(), effectiveDailyCountLimit);

        return new RecipientPreviewDTO(
                recipient.getPrenom() + " " + recipient.getNom(),
                maskIdentifier(identifier),
                primary.getIdCompte(),
                primary.getTypeCompte().name(),
                true,
                recipient.getPhotoUrl(),
                feeRate,
                null,  // estimated fee calculated on frontend from feeRate
                cfg.getLargeTransferThreshold(),
                cfg.getDailyAmountLimit(),
                effectiveDailyCountLimit
        );
    }


    private static final int FREE_MONTHLY_LIMIT    = 10;
    private static final int PREMIUM_MONTHLY_LIMIT  = 50;

    @Override
    public TransferConstraintsDTO getConstraints(Long senderUserId, boolean internal) {
        FraudeConfig cfg = fraudeService.getConfigEntity();

        int effectiveDailyCountLimit = cfg.getDailyCountLimit();
        if (senderUserId != null) {
            Utilisateur sender = utilisateurRepository.findById(senderUserId).orElse(null);
            if (sender != null && sender.isPremium()) {
                effectiveDailyCountLimit = PREMIUM_DAILY_LIMIT;
            }
        }

        double effectiveFeeRate = internal ? 0.0 : feeRate;
        return new TransferConstraintsDTO(
                effectiveFeeRate,
                cfg.getLargeTransferThreshold(),
                cfg.getDailyAmountLimit(),
                effectiveDailyCountLimit
        );
    }

    @Override
    @Transactional
    public VirementResponseDTO effectuerVirement(VirementCreateDTO dto, Long senderUserId) {

        if (dto.getMontant() <= 0) throw new BadRequestException("Le montant doit être supérieur à 0");

        // Idempotency check
        Optional<Virement> existing = virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) return mapToResponseDTO(existing.get());

        // ── Plan-based monthly transfer limit ─────────────────────────────
        Utilisateur sender = utilisateurRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur introuvable"));
        enforceMonthlyLimit(sender);
        // ─────────────────────────────────────────────────────────────────

        // Hard fraud limits
        // so it uses its own @Transactional(readOnly=true) and releases its connection
        // before this write transaction acquires its locks.
        fraudeService.enforceHardLimits(senderUserId, dto.getMontant());

        Compte compteSource = getPrimaryAccount(senderUserId);
        if (compteSource == null)
            throw new BadRequestException("Vous n'avez pas de compte bancaire actif. Veuillez en ouvrir un d'abord.");

        Utilisateur recipient = findByIdentifier(dto.getRecipientIdentifier());

        if (recipient == null)
            throw new ResourceNotFoundException("Aucun utilisateur trouvé avec cet email ou ce numéro de téléphone");

        if (recipient.getIdUtilisateur().equals(senderUserId))
            throw new BadRequestException("Vous ne pouvez pas vous transférer de l'argent à vous-même");

        Compte compteDestination = getPrimaryAccount(recipient.getIdUtilisateur());
        if (compteDestination == null)
            throw new BadRequestException("Le bénéficiaire n'a pas de compte bancaire actif");

        // Calculate tax
        double frais = Math.round(dto.getMontant() * feeRate * 1000.0) / 1000.0;
        double totalDebite = dto.getMontant() + frais;

        // Lock accounts in consistent order (lower id first) to prevent deadlocks
        Long lowId  = Math.min(compteSource.getIdCompte(), compteDestination.getIdCompte());
        Long highId = Math.max(compteSource.getIdCompte(), compteDestination.getIdCompte());

        Compte lockedLow  = compteRepository.findByIdForUpdate(lowId).orElseThrow();
        Compte lockedHigh = compteRepository.findByIdForUpdate(highId).orElseThrow();

        Compte lockedSource = lockedLow.getIdCompte().equals(compteSource.getIdCompte()) ? lockedLow : lockedHigh;
        Compte lockedDest   = lockedLow.getIdCompte().equals(compteDestination.getIdCompte()) ? lockedLow : lockedHigh;

        if (lockedSource.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Votre compte n'est pas actif");
        if (lockedSource.getSolde() < totalDebite)
            throw new BadRequestException("Solde insuffisant (montant + frais : " + totalDebite + " TND)");

        // Debit sender (amount + fee), credit receiver (amount only)
        lockedSource.setSolde(lockedSource.getSolde() - totalDebite);
        lockedDest.setSolde(lockedDest.getSolde() + dto.getMontant());

        compteRepository.save(lockedSource);
        compteRepository.save(lockedDest);

        // Save virement
        Virement virement = new Virement();
        virement.setCompteDe(lockedSource);
        virement.setCompteA(lockedDest);
        virement.setMontant(dto.getMontant());
        virement.setFrais(frais);
        virement.setTauxFrais(feeRate);
        virement.setDateDeVirement(new Date());
        virement.setIdempotencyKey(dto.getIdempotencyKey());
        Virement saved = virementRepository.save(virement);

        // Credit internal fee account + audit record — all in same transaction
        CompteInterne feeAccount = compteInterneRepository.findByNom(feeAccountName)
                .orElseThrow(() -> new IllegalStateException("Compte interne des frais introuvable. Vérifiez DataInitializer."));
        feeAccount.setSolde(feeAccount.getSolde() + frais);
        compteInterneRepository.save(feeAccount);

        FraisTransaction fraisTransaction = new FraisTransaction();
        fraisTransaction.setVirement(saved);
        fraisTransaction.setMontantFrais(frais);
        fraisTransaction.setTauxApplique(feeRate);
        fraisTransactionRepository.save(fraisTransaction);

        // Async fraud monitoring — fires in a separate thread after commit.
        // Uses IDs only to avoid detached-entity issues across thread boundaries.
        // Async fraud monitoring — must fire AFTER the transaction commits so the
        // new Virement is visible in the async thread's own DB session.
        final Long savedId1 = saved.getIdVirement();
        final Long senderId1 = senderUserId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                fraudeService.analyzeTransferAsync(savedId1, senderId1);
                adminEventPublisher.publish(AdminEventPublisher.EventType.VIREMENT);
            }
        });

        // Notifications (after commit via NotificationService)
        notificationService.notifyUser(
                senderUserId,
                NotificationType.TRANSFER_SENT,
                "Virement envoyé",
                "Vous avez envoyé " + dto.getMontant() + " TND à " + recipient.getPrenom() + " " + recipient.getNom() + ".",
                "/virement-view"
        );
        notificationService.notifyUser(
                recipient.getIdUtilisateur(),
                NotificationType.TRANSFER_RECEIVED,
                "Virement reçu",
                "Vous avez reçu " + dto.getMontant() + " TND de " + sender.getPrenom() + " " + sender.getNom() + ".",
                "/virement-view"
        );

        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public VirementResponseDTO effectuerVirementInterne(InternalTransferCreateDTO dto, Long senderUserId) {
        if (dto.getMontant() == null || dto.getMontant() <= 0) {
            throw new BadRequestException("Le montant doit être supérieur à 0");
        }
        if (dto.getCompteSourceId() == null || dto.getCompteDestinationId() == null) {
            throw new BadRequestException("Comptes source/destination invalides");
        }
        if (dto.getCompteSourceId().equals(dto.getCompteDestinationId())) {
            throw new BadRequestException("Veuillez sélectionner deux comptes différents");
        }
        if (dto.getIdempotencyKey() == null || dto.getIdempotencyKey().isBlank()) {
            throw new BadRequestException("Clé d'idempotence invalide");
        }

        Optional<Virement> existing = virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) return mapToResponseDTO(existing.get());

        Utilisateur sender = utilisateurRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Expéditeur introuvable"));
        enforceMonthlyLimit(sender);

        // Hard fraud limits still apply
        fraudeService.enforceHardLimits(senderUserId, dto.getMontant());

        // Lock accounts in consistent order (lower id first) to prevent deadlocks
        Long lowId  = Math.min(dto.getCompteSourceId(), dto.getCompteDestinationId());
        Long highId = Math.max(dto.getCompteSourceId(), dto.getCompteDestinationId());

        Compte lockedLow = compteRepository.findByIdForUpdate(lowId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));
        Compte lockedHigh = compteRepository.findByIdForUpdate(highId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte introuvable"));

        Compte lockedSource = lockedLow.getIdCompte().equals(dto.getCompteSourceId()) ? lockedLow : lockedHigh;
        Compte lockedDest   = lockedLow.getIdCompte().equals(dto.getCompteDestinationId()) ? lockedLow : lockedHigh;

        if (lockedSource.getUtilisateur() == null || !Objects.equals(lockedSource.getUtilisateur().getIdUtilisateur(), senderUserId)) {
            throw new BadRequestException("Compte source invalide");
        }
        if (lockedDest.getUtilisateur() == null || !Objects.equals(lockedDest.getUtilisateur().getIdUtilisateur(), senderUserId)) {
            throw new BadRequestException("Compte destination invalide");
        }
        if (lockedSource.getStatutCompte() != StatutCompte.ACTIVE || lockedDest.getStatutCompte() != StatutCompte.ACTIVE) {
            throw new BadRequestException("Vos comptes doivent être actifs pour effectuer un transfert interne");
        }

        double montant = dto.getMontant();
        if (lockedSource.getSolde() < montant) {
            throw new BadRequestException("Solde insuffisant (montant : " + montant + " TND)");
        }

        lockedSource.setSolde(lockedSource.getSolde() - montant);
        lockedDest.setSolde(lockedDest.getSolde() + montant);
        compteRepository.save(lockedSource);
        compteRepository.save(lockedDest);

        Virement virement = new Virement();
        virement.setCompteDe(lockedSource);
        virement.setCompteA(lockedDest);
        virement.setMontant(montant);
        virement.setFrais(0.0);
        virement.setTauxFrais(0.0);
        virement.setDateDeVirement(new Date());
        virement.setIdempotencyKey(dto.getIdempotencyKey());
        Virement saved = virementRepository.save(virement);

        // Async fraud monitoring — must fire AFTER the transaction commits
        final Long savedId2 = saved.getIdVirement();
        final Long senderId2 = senderUserId;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                fraudeService.analyzeTransferAsync(savedId2, senderId2);
                adminEventPublisher.publish(AdminEventPublisher.EventType.VIREMENT);
            }
        });

        notificationService.notifyUser(
                senderUserId,
                NotificationType.TRANSFER_SENT,
                "Transfert interne effectué",
                "Vous avez transféré " + montant + " TND du compte #" + lockedSource.getIdCompte()
                        + " vers le compte #" + lockedDest.getIdCompte() + ".",
                "/virement-view"
        );

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
                .map(this::mapToResponseDTO)
                .toList();
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

    // ── Helpers ──

    private Compte getPrimaryAccount(Long userId) {
        List<Compte> courants = compteRepository
                .findByUtilisateur_IdUtilisateurAndTypeCompteAndStatutCompteOrderByDateCreationAsc(
                        userId, TypeCompte.COURANT, StatutCompte.ACTIVE);
        if (!courants.isEmpty()) return courants.get(0);
        List<Compte> any = compteRepository
                .findByUtilisateur_IdUtilisateurAndStatutCompteOrderByDateCreationAsc(userId, StatutCompte.ACTIVE);
        return any.isEmpty() ? null : any.get(0);
    }

    private Utilisateur findByIdentifier(String identifier) {
        if (identifier == null) return null;
        String trimmed = identifier.trim();
        Utilisateur byEmail = utilisateurRepository.findByEmail(trimmed);
        return byEmail != null ? byEmail : utilisateurRepository.findByTelephone(trimmed);
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null) return null;
        if (identifier.contains("@")) {
            int at = identifier.indexOf('@');
            String local = identifier.substring(0, at);
            String domain = identifier.substring(at);
            return (local.length() <= 2 ? local : local.substring(0, 2)) + "***" + domain;
        }
        return identifier.length() <= 4 ? "****"
                : "*".repeat(identifier.length() - 4) + identifier.substring(identifier.length() - 4);
    }

    private void enforceMonthlyLimit(Utilisateur sender) {
        Date monthStart = Date.from(LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        long usedThisMonth = virementRepository.countOutgoingSince(sender.getIdUtilisateur(), monthStart);
        int limit = sender.isPremium() ? PREMIUM_MONTHLY_LIMIT : FREE_MONTHLY_LIMIT;
        if (usedThisMonth >= limit) {
            throw new BadRequestException(
                    "Limite mensuelle de virements atteinte (" + usedThisMonth + "/" + limit + "). "
                            + (sender.isPremium()
                            ? "Vous êtes abonné Premium. Merci de contacter la BTE pour plus d'assistance."
                            : "Passez à Premium pour 50 virements par mois. Rendez-vous dans l'agence BTE la plus proche."));
        }
    }

    private VirementResponseDTO mapToResponseDTO(Virement v) {
        Utilisateur sender    = v.getCompteDe().getUtilisateur();
        Utilisateur recipient = v.getCompteA().getUtilisateur();
        double frais = v.getFrais() != null ? v.getFrais() : 0.0;
        return new VirementResponseDTO(
                v.getIdVirement(),
                v.getCompteDe().getIdCompte(),
                v.getCompteA().getIdCompte(),
                recipient != null ? recipient.getPrenom() + " " + recipient.getNom() : "—",
                sender    != null ? sender.getPrenom()    + " " + sender.getNom()    : "—",
                v.getMontant(),
                frais,
                v.getMontant() + frais,
                v.getTauxFrais(),
                v.getDateDeVirement(),
                v.getIdempotencyKey()
        );
    }
}