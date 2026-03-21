package com.sesame.neobte.Services.Other;

import com.sesame.neobte.Entities.Class.ReferralReward;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.IReferralRewardRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.NotificationService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReferralService {

    private static final int PREMIUM_REWARD_DAYS = 3;

    private final IReferralRewardRepository referralRepository;
    private final IUtilisateurRepository utilisateurRepository;

    @Transactional
    public String ensureReferralCode(Utilisateur user) {
        if (user.getReferralCode() != null) return user.getReferralCode();
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        } while (utilisateurRepository.findByReferralCode(code).isPresent());
        user.setReferralCode(code);
        utilisateurRepository.save(user);
        return code;
    }

    /**
     * Completely self-contained referral application.
     * No notifications, no WebSocket, no nested transactions.
     * Throws BadRequestException with a French user-facing message on failure.
     */
    @Transactional
    public String applyReferralCode(Long newUserId, String rawCode) {

        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Le code de parrainage est vide.");
        }

        String code = rawCode.trim().toUpperCase();

        Utilisateur newUser = utilisateurRepository.findById(newUserId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));

        if (referralRepository.existsByReferred_IdUtilisateur(newUserId)) {
            throw new BadRequestException("Ce compte a déjà bénéficié d'un parrainage.");
        }

        Utilisateur referrer = utilisateurRepository.findByReferralCode(code)
                .orElseThrow(() -> new BadRequestException(
                        "Code \"" + code + "\" introuvable. Vérifiez le code et réessayez."));

        if (referrer.getIdUtilisateur().equals(newUserId)) {
            throw new BadRequestException("Vous ne pouvez pas utiliser votre propre code.");
        }

        // Save the referral record
        ReferralReward reward = ReferralReward.builder()
                .referrer(referrer)
                .referred(newUser)
                .codeUsed(code)
                .rewarded(true)
                .build();
        referralRepository.save(reward);

        // Extend or grant Premium to the referrer
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentExpiry = referrer.getPremiumExpiresAt();

        if (referrer.isPremium() && currentExpiry != null && currentExpiry.isAfter(now)) {
            referrer.setPremiumExpiresAt(currentExpiry.plusDays(PREMIUM_REWARD_DAYS));
        } else {
            referrer.setPremium(true);
            referrer.setPremiumExpiresAt(now.plusDays(PREMIUM_REWARD_DAYS));
        }
        utilisateurRepository.save(referrer);

        log.info("[REFERRAL] '{}' used by {} (new) -> {} (referrer) gets {} days Premium.",
                code, newUser.getEmail(), referrer.getEmail(), PREMIUM_REWARD_DAYS);

        return String.format("Parrainage appliqué ! %s %s gagne %d jours Premium.",
                referrer.getPrenom(), referrer.getNom(), PREMIUM_REWARD_DAYS);
    }

    public List<ReferralReward> getReferralsForUser(Long userId) {
        return referralRepository.findByReferrer_IdUtilisateurOrderByDateReferralDesc(userId);
    }

    public long countReferrals(Long userId) {
        return referralRepository.countByReferrer_IdUtilisateur(userId);
    }
}