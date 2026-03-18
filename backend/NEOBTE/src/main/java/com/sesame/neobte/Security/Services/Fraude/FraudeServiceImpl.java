package com.sesame.neobte.Security.Services.Fraude;

import com.sesame.neobte.DTO.Requests.Fraude.FraudeConfigUpdateDTO;
import com.sesame.neobte.DTO.Requests.Fraude.FraudeReviewDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeAlerteResponseDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeConfigResponseDTO;

import com.sesame.neobte.Entities.Class.Fraude.FraudeAlerte;
import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Class.Virement;

import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeAlertType;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeSeverity;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;

import com.sesame.neobte.Repositories.Fraude.IFraudeAlerteRepository;
import com.sesame.neobte.Repositories.Fraude.IFraudeConfigRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import com.sesame.neobte.Security.Services.Fraude.FraudeService;
import com.sesame.neobte.Services.Other.AdminEventPublisher;
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

    private static final int PREMIUM_DAILY_COUNT_LIMIT = 50;

    private final IFraudeAlerteRepository alerteRepository;
    private final IFraudeConfigRepository configRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IVirementRepository virementRepository;
    private final EmailService emailService;
    private final AdminEventPublisher adminEventPublisher;

    // ─────────────────────────────────────────────────────────────────────────
    // PRE-FLIGHT: hard limits
    // Called BEFORE effectuerVirement opens @Transactional.
    // Uses its own short read-only queries — no connection held by caller.
    // ─────────────────────────────────────────────────────────────────────────
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enforceHardLimits(Long senderUserId, double montant) {
        FraudeConfig cfg = getOrCreateConfig();
        Utilisateur sender = utilisateurRepository.findById(senderUserId).orElse(null);
        int effectiveDailyCountLimit = (sender != null && sender.isPremium())
                ? PREMIUM_DAILY_COUNT_LIMIT
                : cfg.getDailyCountLimit();

        // 1. Single transfer hard cap
        if (montant > cfg.getLargeTransferThreshold()) {
            if (sender != null) {
                alerteRepository.save(buildAlerte(
                        FraudeAlertType.LARGE_SINGLE_TRANSFER,
                        FraudeSeverity.HIGH,
                        String.format(
                                "Tentative de virement de %.3f TND au-dessus du seuil autorisé (%.3f TND).",
                                montant, cfg.getLargeTransferThreshold()),
                        sender,
                        null
                ));
            }
            throw new BadRequestException(String.format(
                    "Montant de %.3f TND dépasse la limite par virement de %.3f TND. " +
                            "Contactez le support pour envoyer des montants plus importants.",
                    montant, cfg.getLargeTransferThreshold()));
        }

        // 2. Daily count
        Date since24h = since(24 * 60);
        long countToday = alerteRepository.countTransfersSince(senderUserId, since24h);
        if (countToday >= effectiveDailyCountLimit) {
            if (sender != null) {
                alerteRepository.save(buildAlerte(
                        FraudeAlertType.DAILY_COUNT_EXCEEDED,
                        FraudeSeverity.HIGH,
                        String.format(
                                "Limite de nombre de virements sur 24h dépassée (%d/%d).",
                                countToday, effectiveDailyCountLimit),
                        sender,
                        null
                ));
            }
            throw new BadRequestException(String.format(
                    "Limite journalière atteinte : %d virements maximum par 24h. " +
                            "Votre limite sera réinitialisée dans les prochaines heures.",
                    effectiveDailyCountLimit));
        }

        // 3. Daily amount
        Number rawSum = alerteRepository.sumAmountSinceAsNumber(senderUserId, since24h);
        double sentToday = rawSum != null ? rawSum.doubleValue() : 0.0;
        if (sentToday + montant > cfg.getDailyAmountLimit()) {
            double remaining = Math.max(0.0, cfg.getDailyAmountLimit() - sentToday);
            if (sender != null) {
                alerteRepository.save(buildAlerte(
                        FraudeAlertType.DAILY_AMOUNT_EXCEEDED,
                        FraudeSeverity.HIGH,
                        String.format(
                                "Limite de montant sur 24h dépassée (%.3f/%.3f TND). Tentative: %.3f TND.",
                                sentToday, cfg.getDailyAmountLimit(), montant),
                        sender,
                        null
                ));
            }
            throw new BadRequestException(String.format(
                    "Ce virement dépasserait votre limite journalière de %.3f TND. " +
                            "Solde disponible aujourd'hui : %.3f TND.",
                    cfg.getDailyAmountLimit(), remaining));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ASYNC MONITORING — runs in a separate thread AFTER the transfer commits.
    // Takes IDs, not entities, to avoid detached-object issues across threads.
    // Checks large transfer, suspicious hour, and rapid succession.
    // ─────────────────────────────────────────────────────────────────────────
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

            // 1. Large single transfer — alert (the hard block already prevented amounts OVER the limit,
            //    but transfers AT or near the threshold that passed are still suspicious)
            double nearThresholdPct = 0.80; // flag anything >= 80% of the threshold
            if (virement.getMontant() >= cfg.getLargeTransferThreshold() * nearThresholdPct) {
                raised.add(buildAlerte(
                        FraudeAlertType.LARGE_SINGLE_TRANSFER,
                        virement.getMontant() >= cfg.getLargeTransferThreshold() * 0.95
                                ? FraudeSeverity.HIGH : FraudeSeverity.MEDIUM,
                        String.format(
                                "Transfer of %.3f TND is %.0f%% of the single-transfer limit (%.3f TND).",
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
                        String.format(
                                "Transfer of %.3f TND at %02d:%02d — outside normal hours (%02d:00–%02d:00).",
                                virement.getMontant(),
                                LocalDateTime.now().getHour(), LocalDateTime.now().getMinute(),
                                cfg.getSuspiciousHourStart(), cfg.getSuspiciousHourEnd()),
                        sender, virement));
            }

            // 3. Rapid succession — count transfers in last N minutes.
            // Safe to query directly: afterCommit() guarantees the transfer is visible in DB.
            Date sinceWindow = since(cfg.getRapidSuccessionMinutes());
            long recentCount = virementRepository.countOutgoingSince(senderUserId, sinceWindow);
            if (recentCount >= cfg.getRapidSuccessionCount()) {
                raised.add(buildAlerte(
                        FraudeAlertType.RAPID_SUCCESSION, FraudeSeverity.HIGH,
                        String.format(
                                "%d transfers in %d minutes — possible automated bot activity.",
                                recentCount, cfg.getRapidSuccessionMinutes()),
                        sender, virement));
            }

            if (raised.isEmpty()) return;

            alerteRepository.saveAll(raised);
            adminEventPublisher.publish(AdminEventPublisher.EventType.FRAUDE);
            log.warn("[FRAUD] {} alert(s) for user {} ({})",
                    raised.size(), senderUserId, sender.getEmail());

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

    @Override
    public FraudeConfig getConfigEntity() {
        return getOrCreateConfig();
    }

    // ── Admin reads ──────────────────────────────────────────────────────────
    @Override
    public List<FraudeAlerteResponseDTO> getAllAlertes() {
        return alerteRepository.findAllByOrderByDateAlerteDesc()
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public List<FraudeAlerteResponseDTO> getOpenAlertes() {
        return alerteRepository.findByStatutOrderByDateAlerteDesc(FraudeStatut.OPEN)
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    @Transactional
    public FraudeAlerteResponseDTO reviewAlerte(Long id, FraudeReviewDTO dto) {
        FraudeAlerte alerte = alerteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte introuvable"));
        FraudeStatut newStatut;
        try {
            newStatut = FraudeStatut.valueOf(dto.getNewStatut());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide : " + dto.getNewStatut());
        }
        alerte.setStatut(newStatut);
        if (dto.getAdminNote() != null) alerte.setAdminNote(dto.getAdminNote());
        alerte.setDateRevue(LocalDateTime.now());
        return mapToDTO(alerteRepository.save(alerte));
    }

    @Override
    public long countOpen() {
        return alerteRepository.countByStatut(FraudeStatut.OPEN);
    }

    // ── Config ───────────────────────────────────────────────────────────────
    @Override
    public FraudeConfigResponseDTO getConfig() {
        return mapConfigToDTO(getOrCreateConfig());
    }

    @Override
    @Transactional
    public FraudeConfigResponseDTO updateConfig(FraudeConfigUpdateDTO dto) {
        FraudeConfig cfg = getOrCreateConfig();
        cfg.setDailyCountLimit(dto.getDailyCountLimit());
        cfg.setDailyAmountLimit(dto.getDailyAmountLimit());
        cfg.setLargeTransferThreshold(dto.getLargeTransferThreshold());
        cfg.setRapidSuccessionCount(dto.getRapidSuccessionCount());
        cfg.setRapidSuccessionMinutes(dto.getRapidSuccessionMinutes());
        cfg.setSuspiciousHourStart(dto.getSuspiciousHourStart());
        cfg.setSuspiciousHourEnd(dto.getSuspiciousHourEnd());
        cfg.setEmailAlertsEnabled(dto.isEmailAlertsEnabled());
        return mapConfigToDTO(configRepository.save(cfg));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private FraudeConfig getOrCreateConfig() {
        return configRepository.findById(1L).orElseGet(() -> {
            FraudeConfig cfg = new FraudeConfig();
            cfg.setId(1L);
            return configRepository.save(cfg);
        });
    }

    private Date since(int minutes) {
        return Date.from(LocalDateTime.now().minusMinutes(minutes)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    private FraudeAlerte buildAlerte(FraudeAlertType type, FraudeSeverity severity,
                                     String description, Utilisateur user, Virement virement) {
        FraudeAlerte a = new FraudeAlerte();
        a.setType(type);
        a.setSeverity(severity);
        a.setStatut(FraudeStatut.OPEN);
        a.setDescription(description);
        a.setUtilisateur(user);
        a.setVirement(virement);
        return a;
    }

    private FraudeAlerteResponseDTO mapToDTO(FraudeAlerte a) {
        return FraudeAlerteResponseDTO.builder()
                .id(a.getId())
                .type(a.getType().name())
                .severity(a.getSeverity().name())
                .statut(a.getStatut().name())
                .description(a.getDescription())
                .adminNote(a.getAdminNote())
                .utilisateurId(a.getUtilisateur().getIdUtilisateur())
                .utilisateurNom(a.getUtilisateur().getPrenom() + " " + a.getUtilisateur().getNom())
                .utilisateurEmail(a.getUtilisateur().getEmail())
                .virementId(a.getVirement() != null ? a.getVirement().getIdVirement() : null)
                .virementMontant(a.getVirement() != null ? a.getVirement().getMontant() : null)
                .dateAlerte(a.getDateAlerte())
                .dateRevue(a.getDateRevue())
                .build();
    }

    private FraudeConfigResponseDTO mapConfigToDTO(FraudeConfig cfg) {
        return FraudeConfigResponseDTO.builder()
                .dailyCountLimit(cfg.getDailyCountLimit())
                .dailyAmountLimit(cfg.getDailyAmountLimit())
                .largeTransferThreshold(cfg.getLargeTransferThreshold())
                .rapidSuccessionCount(cfg.getRapidSuccessionCount())
                .rapidSuccessionMinutes(cfg.getRapidSuccessionMinutes())
                .suspiciousHourStart(cfg.getSuspiciousHourStart())
                .suspiciousHourEnd(cfg.getSuspiciousHourEnd())
                .emailAlertsEnabled(cfg.isEmailAlertsEnabled())
                .build();
    }
}