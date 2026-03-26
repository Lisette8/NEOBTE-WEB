package com.sesame.neobte.Security.Services.Fraude;

import com.sesame.neobte.Config.AccountTypePolicy;
import com.sesame.neobte.DTO.Requests.Fraude.FraudeConfigUpdateDTO;
import com.sesame.neobte.DTO.Requests.Fraude.FraudeReviewDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeAlerteResponseDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeConfigResponseDTO;

import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Fraude.FraudeAlerte;
import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Class.Virement;

import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeAlertType;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeSeverity;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;

import com.sesame.neobte.Repositories.Fraude.IFraudeAlerteRepository;
import com.sesame.neobte.Repositories.Fraude.IFraudeConfigRepository;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FraudeServiceImpl implements FraudeService {

    private final IFraudeAlerteRepository alerteRepository;
    private final IFraudeConfigRepository configRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final ICompteRepository compteRepository;
    private final IVirementRepository virementRepository;
    private final EmailService emailService;

    /**
     * Pre-flight hard limits — called before effectuerVirement opens its @Transactional.
     * Uses the FraudeConfig largeTransferThreshold as an absolute ceiling (overrides per-type).
     * Per-type daily limits are enforced in VirementServiceImpl using AccountTypePolicy.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enforceHardLimits(Long senderUserId, double montant) {
        FraudeConfig cfg = getOrCreateConfig();
        Utilisateur sender = utilisateurRepository.findById(senderUserId).orElse(null);

        // 1. Absolute single-transfer ceiling from fraud config
        if (montant > cfg.getLargeTransferThreshold()) {
            if (sender != null) alerteRepository.save(buildAlerte(
                    FraudeAlertType.LARGE_SINGLE_TRANSFER, FraudeSeverity.HIGH,
                    String.format("Tentative de virement de %.3f TND au-dessus du seuil autorisé (%.3f TND).",
                            montant, cfg.getLargeTransferThreshold()),
                    sender, null));
            throw new BadRequestException(String.format(
                    "Ce virement de %.3f TND dépasse la limite absolue par virement de %.3f TND. " +
                            "Contactez le support pour des montants exceptionnels.",
                    montant, cfg.getLargeTransferThreshold()));
        }

        // 2 & 3: Per-account-type daily count and amount limits
        //    These are enforced here as a hard-block (with fraud alert), in addition to
        //    the policy checks in VirementServiceImpl which give cleaner user messages.
        Compte sourceAccount = getPrimaryActiveAccount(senderUserId);
        if (sourceAccount == null) return; // account checks happen later in VirementService

        TypeCompte sourceType = sourceAccount.getTypeCompte();
        Date since24h = since(24 * 60);

        long countToday = virementRepository.countOutgoingFromCompteSince(sourceAccount.getIdCompte(), since24h);
        int dailyCountLimit = AccountTypePolicy.dailyCountLimit(sourceType, cfg);
        if (countToday >= dailyCountLimit) {
            if (sender != null) alerteRepository.save(buildAlerte(
                    FraudeAlertType.DAILY_COUNT_EXCEEDED, FraudeSeverity.HIGH,
                    String.format("Limite journalière atteinte (%d/%d virements) sur %s #%d.",
                            countToday, dailyCountLimit, AccountTypePolicy.label(sourceType),
                            sourceAccount.getIdCompte()),
                    sender, null));
            throw new BadRequestException(String.format(
                    "Vous avez atteint la limite de %d virements par jour pour votre %s. " +
                            "Votre compteur sera réinitialisé à minuit.",
                    dailyCountLimit, AccountTypePolicy.label(sourceType)));
        }

        Double sentToday = virementRepository.sumOutgoingFromCompteSince(sourceAccount.getIdCompte(), since24h);
        double alreadySent = sentToday != null ? sentToday : 0.0;
        double dailyAmountLimit = AccountTypePolicy.dailyAmountLimit(sourceType, cfg);
        if (alreadySent + montant > dailyAmountLimit) {
            double remaining = Math.max(0.0, dailyAmountLimit - alreadySent);
            if (sender != null) alerteRepository.save(buildAlerte(
                    FraudeAlertType.DAILY_AMOUNT_EXCEEDED, FraudeSeverity.HIGH,
                    String.format("Limite journalière de montant dépassée (%.3f/%.3f TND) sur %s #%d.",
                            alreadySent + montant, dailyAmountLimit,
                            AccountTypePolicy.label(sourceType), sourceAccount.getIdCompte()),
                    sender, null));
            throw new BadRequestException(String.format(
                    "Ce virement dépasserait votre limite journalière de %.3f TND pour votre %s. " +
                            "Capacité restante aujourd'hui : %.3f TND.",
                    dailyAmountLimit, AccountTypePolicy.label(sourceType), remaining));
        }
    }

    @Override
    @Async
    @Transactional
    public void analyzeTransferAsync(Long virementId, Long senderUserId) {
        try {
            Virement virement = virementRepository.findById(virementId).orElse(null);
            Utilisateur sender = utilisateurRepository.findById(senderUserId).orElse(null);
            if (virement == null || sender == null) return;

            FraudeConfig cfg = getOrCreateConfig();
            List<FraudeAlerte> raised = new ArrayList<>();

            // 1. Near-threshold large transfer
            if (virement.getMontant() >= cfg.getLargeTransferThreshold() * 0.80) {
                raised.add(buildAlerte(
                        FraudeAlertType.LARGE_SINGLE_TRANSFER,
                        virement.getMontant() >= cfg.getLargeTransferThreshold() * 0.95
                                ? FraudeSeverity.HIGH : FraudeSeverity.MEDIUM,
                        String.format("Virement de %.3f TND représente %.0f%% du seuil (%.3f TND).",
                                virement.getMontant(),
                                (virement.getMontant() / cfg.getLargeTransferThreshold()) * 100,
                                cfg.getLargeTransferThreshold()),
                        sender, virement));
            }

            // 2. Suspicious hour
            int hour = LocalDateTime.now().getHour();
            if (hour >= cfg.getSuspiciousHourStart() && hour < cfg.getSuspiciousHourEnd()) {
                raised.add(buildAlerte(
                        FraudeAlertType.SUSPICIOUS_HOUR, FraudeSeverity.MEDIUM,
                        String.format("Virement de %.3f TND à %02d:%02d (heure suspecte %02d:00–%02d:00).",
                                virement.getMontant(),
                                LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(),
                                cfg.getSuspiciousHourStart(), cfg.getSuspiciousHourEnd()),
                        sender, virement));
            }

            // 3. Rapid succession
            Date sinceWindow = since(cfg.getRapidSuccessionMinutes());
            long recentCount = virementRepository.countOutgoingSince(senderUserId, sinceWindow);
            if (recentCount >= cfg.getRapidSuccessionCount()) {
                raised.add(buildAlerte(
                        FraudeAlertType.RAPID_SUCCESSION, FraudeSeverity.HIGH,
                        String.format("%d virements en %d minutes — possible activité automatisée.",
                                recentCount, cfg.getRapidSuccessionMinutes()),
                        sender, virement));
            }

            if (raised.isEmpty()) return;
            alerteRepository.saveAll(raised);
            log.warn("[FRAUD] {} alerte(s) pour utilisateur {} ({})", raised.size(), senderUserId, sender.getEmail());

            if (cfg.isEmailAlertsEnabled()) {
                utilisateurRepository.findAll().stream()
                        .filter(u -> u.getRole() == Role.ADMIN)
                        .forEach(admin -> raised.forEach(a ->
                                emailService.sendFraudeAlertEmail(
                                        admin.getEmail(), admin.getPrenom(),
                                        sender.getPrenom() + " " + sender.getNom(),
                                        sender.getEmail(),
                                        a.getType().name(), a.getSeverity().name(),
                                        a.getDescription())));
            }
        } catch (Exception e) {
            log.error("[FRAUD] analyzeTransferAsync error: {}", e.getMessage(), e);
        }
    }

    @Override public FraudeConfig getConfigEntity() { return getOrCreateConfig(); }

    @Override public List<FraudeAlerteResponseDTO> getAllAlertes() {
        return alerteRepository.findAllByOrderByDateAlerteDesc().stream().map(this::mapToDTO).toList();
    }
    @Override public List<FraudeAlerteResponseDTO> getOpenAlertes() {
        return alerteRepository.findByStatutOrderByDateAlerteDesc(FraudeStatut.OPEN).stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public FraudeAlerteResponseDTO reviewAlerte(Long id, FraudeReviewDTO dto) {
        FraudeAlerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte introuvable"));
        try { alerte.setStatut(FraudeStatut.valueOf(dto.getNewStatut())); }
        catch (IllegalArgumentException e) { throw new BadRequestException("Statut invalide : " + dto.getNewStatut()); }
        if (dto.getAdminNote() != null) alerte.setAdminNote(dto.getAdminNote());
        alerte.setDateRevue(LocalDateTime.now());
        return mapToDTO(alerteRepository.save(alerte));
    }

    @Override public long countOpen() { return alerteRepository.countByStatut(FraudeStatut.OPEN); }

    @Override public FraudeConfigResponseDTO getConfig() { return mapConfigToDTO(getOrCreateConfig()); }

    @Override
    @Transactional
    public FraudeConfigResponseDTO updateConfig(FraudeConfigUpdateDTO dto) {
        FraudeConfig cfg = getOrCreateConfig();
        // Fraud detection
        cfg.setLargeTransferThreshold(dto.getLargeTransferThreshold());
        cfg.setRapidSuccessionCount(dto.getRapidSuccessionCount());
        cfg.setRapidSuccessionMinutes(dto.getRapidSuccessionMinutes());
        cfg.setSuspiciousHourStart(dto.getSuspiciousHourStart());
        cfg.setSuspiciousHourEnd(dto.getSuspiciousHourEnd());
        cfg.setEmailAlertsEnabled(dto.isEmailAlertsEnabled());
        // Fee rates
        cfg.setCourantFeeRate(dto.getCourantFeeRate());
        cfg.setEpargneFeeRate(dto.getEpargneFeeRate());
        cfg.setProfessionnelFeeRate(dto.getProfessionnelFeeRate());
        // COURANT limits
        cfg.setCourantDailyAmountLimit(dto.getCourantDailyAmountLimit());
        cfg.setCourantDailyCountLimit(dto.getCourantDailyCountLimit());
        cfg.setCourantMonthlyCountLimit(dto.getCourantMonthlyCountLimit());
        cfg.setCourantMaxTransfer(dto.getCourantMaxTransfer());
        // EPARGNE limits
        cfg.setEpargneDailyAmountLimit(dto.getEpargneDailyAmountLimit());
        cfg.setEpargneDailyCountLimit(dto.getEpargneDailyCountLimit());
        cfg.setEpargneMonthlyCountLimit(dto.getEpargneMonthlyCountLimit());
        cfg.setEpargneMaxTransfer(dto.getEpargneMaxTransfer());
        // PROFESSIONNEL limits
        cfg.setProfessionnelDailyAmountLimit(dto.getProfessionnelDailyAmountLimit());
        cfg.setProfessionnelDailyCountLimit(dto.getProfessionnelDailyCountLimit());
        cfg.setProfessionnelMonthlyCountLimit(dto.getProfessionnelMonthlyCountLimit());
        cfg.setProfessionnelMaxTransfer(dto.getProfessionnelMaxTransfer());
        return mapConfigToDTO(configRepository.save(cfg));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private FraudeConfig getOrCreateConfig() {
        return configRepository.findById(1L).orElseGet(() -> {
            FraudeConfig c = new FraudeConfig(); c.setId(1L); return configRepository.save(c);
        });
    }

    private Compte getPrimaryActiveAccount(Long userId) {
        for (TypeCompte t : List.of(TypeCompte.COURANT, TypeCompte.PROFESSIONNEL, TypeCompte.EPARGNE)) {
            List<Compte> list = compteRepository
                    .findByUtilisateur_IdUtilisateurAndTypeCompteAndStatutCompteOrderByDateCreationAsc(
                            userId, t, StatutCompte.ACTIVE);
            if (!list.isEmpty()) return list.get(0);
        }
        return null;
    }

    private Date since(int minutes) {
        return Date.from(LocalDateTime.now().minusMinutes(minutes).atZone(ZoneId.systemDefault()).toInstant());
    }

    private FraudeAlerte buildAlerte(FraudeAlertType type, FraudeSeverity severity,
                                     String description, Utilisateur user, Virement virement) {
        FraudeAlerte a = new FraudeAlerte();
        a.setType(type); a.setSeverity(severity); a.setStatut(FraudeStatut.OPEN);
        a.setDescription(description); a.setUtilisateur(user); a.setVirement(virement);
        return a;
    }

    private FraudeAlerteResponseDTO mapToDTO(FraudeAlerte a) {
        return FraudeAlerteResponseDTO.builder()
                .id(a.getId()).type(a.getType().name()).severity(a.getSeverity().name())
                .statut(a.getStatut().name()).description(a.getDescription()).adminNote(a.getAdminNote())
                .utilisateurId(a.getUtilisateur().getIdUtilisateur())
                .utilisateurNom(a.getUtilisateur().getPrenom() + " " + a.getUtilisateur().getNom())
                .utilisateurEmail(a.getUtilisateur().getEmail())
                .virementId(a.getVirement() != null ? a.getVirement().getIdVirement() : null)
                .virementMontant(a.getVirement() != null ? a.getVirement().getMontant() : null)
                .dateAlerte(a.getDateAlerte()).dateRevue(a.getDateRevue()).build();
    }

    private FraudeConfigResponseDTO mapConfigToDTO(FraudeConfig c) {
        return FraudeConfigResponseDTO.builder()
                .largeTransferThreshold(c.getLargeTransferThreshold())
                .rapidSuccessionCount(c.getRapidSuccessionCount())
                .rapidSuccessionMinutes(c.getRapidSuccessionMinutes())
                .suspiciousHourStart(c.getSuspiciousHourStart())
                .suspiciousHourEnd(c.getSuspiciousHourEnd())
                .emailAlertsEnabled(c.isEmailAlertsEnabled())
                .courantFeeRate(c.getCourantFeeRate())
                .epargneFeeRate(c.getEpargneFeeRate())
                .professionnelFeeRate(c.getProfessionnelFeeRate())
                .courantDailyAmountLimit(c.getCourantDailyAmountLimit())
                .courantDailyCountLimit(c.getCourantDailyCountLimit())
                .courantMonthlyCountLimit(c.getCourantMonthlyCountLimit())
                .courantMaxTransfer(c.getCourantMaxTransfer())
                .epargneDailyAmountLimit(c.getEpargneDailyAmountLimit())
                .epargneDailyCountLimit(c.getEpargneDailyCountLimit())
                .epargneMonthlyCountLimit(c.getEpargneMonthlyCountLimit())
                .epargneMaxTransfer(c.getEpargneMaxTransfer())
                .professionnelDailyAmountLimit(c.getProfessionnelDailyAmountLimit())
                .professionnelDailyCountLimit(c.getProfessionnelDailyCountLimit())
                .professionnelMonthlyCountLimit(c.getProfessionnelMonthlyCountLimit())
                .professionnelMaxTransfer(c.getProfessionnelMaxTransfer())
                .build();
    }
}