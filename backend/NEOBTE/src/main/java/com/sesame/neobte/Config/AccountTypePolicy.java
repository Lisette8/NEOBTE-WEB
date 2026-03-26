package com.sesame.neobte.Config;

import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;

/**
 * Single source of truth for per-account-type banking rules.
 *
 * Limits and fee rates are now stored in FraudeConfig (one DB row, admin-editable).
 * Static fallback values are used only when no config is available.
 *
 * All limit methods accept an optional FraudeConfig; pass null to use hardcoded defaults.
 */
public class AccountTypePolicy {

    // ── Static fallbacks (used only if FraudeConfig is unavailable) ────────

    public static double maxTransferAmount(TypeCompte type) {
        return switch (type) {
            case COURANT       ->  5_000.0;
            case EPARGNE       ->  1_000.0;
            case PROFESSIONNEL -> 50_000.0;
        };
    }

    public static double maxTransferAmount(TypeCompte type, FraudeConfig cfg) {
        if (cfg == null) return maxTransferAmount(type);
        return switch (type) {
            case COURANT       -> cfg.getCourantMaxTransfer();
            case EPARGNE       -> cfg.getEpargneMaxTransfer();
            case PROFESSIONNEL -> cfg.getProfessionnelMaxTransfer();
        };
    }

    public static double dailyAmountLimit(TypeCompte type) {
        return switch (type) {
            case COURANT       ->  10_000.0;
            case EPARGNE       ->   2_000.0;
            case PROFESSIONNEL -> 100_000.0;
        };
    }

    public static double dailyAmountLimit(TypeCompte type, FraudeConfig cfg) {
        if (cfg == null) return dailyAmountLimit(type);
        return switch (type) {
            case COURANT       -> cfg.getCourantDailyAmountLimit();
            case EPARGNE       -> cfg.getEpargneDailyAmountLimit();
            case PROFESSIONNEL -> cfg.getProfessionnelDailyAmountLimit();
        };
    }

    public static int dailyCountLimit(TypeCompte type) {
        return switch (type) {
            case COURANT       -> 10;
            case EPARGNE       ->  3;
            case PROFESSIONNEL -> 50;
        };
    }

    public static int dailyCountLimit(TypeCompte type, FraudeConfig cfg) {
        if (cfg == null) return dailyCountLimit(type);
        return switch (type) {
            case COURANT       -> cfg.getCourantDailyCountLimit();
            case EPARGNE       -> cfg.getEpargneDailyCountLimit();
            case PROFESSIONNEL -> cfg.getProfessionnelDailyCountLimit();
        };
    }

    public static int monthlyCountLimit(TypeCompte type) {
        return switch (type) {
            case COURANT       ->  30;
            case EPARGNE       ->   5;
            case PROFESSIONNEL -> 500;
        };
    }

    public static int monthlyCountLimit(TypeCompte type, FraudeConfig cfg) {
        if (cfg == null) return monthlyCountLimit(type);
        return switch (type) {
            case COURANT       -> cfg.getCourantMonthlyCountLimit();
            case EPARGNE       -> cfg.getEpargneMonthlyCountLimit();
            case PROFESSIONNEL -> cfg.getProfessionnelMonthlyCountLimit();
        };
    }

    public static double feeRate(TypeCompte type) {
        return switch (type) {
            case COURANT       -> 0.005;
            case EPARGNE       -> 0.010;
            case PROFESSIONNEL -> 0.003;
        };
    }

    public static double feeRate(TypeCompte type, FraudeConfig cfg) {
        if (cfg == null) return feeRate(type);
        return switch (type) {
            case COURANT       -> cfg.getCourantFeeRate();
            case EPARGNE       -> cfg.getEpargneFeeRate();
            case PROFESSIONNEL -> cfg.getProfessionnelFeeRate();
        };
    }

    // ── Annual interest ─────────────────────────────────────────────────────

    public static double annualInterestRate(TypeCompte type) {
        return switch (type) {
            case EPARGNE                    -> 0.045;
            case COURANT, PROFESSIONNEL     -> 0.0;
        };
    }

    // ── Permissions ─────────────────────────────────────────────────────────

    public static boolean canSendExternal(TypeCompte type) {
        return switch (type) {
            case COURANT, PROFESSIONNEL -> true;
            case EPARGNE                -> false;
        };
    }

    public static boolean canSendInternal(TypeCompte type) { return true; }
    public static boolean canReceive(TypeCompte type)      { return true; }

    // ── Enforcement helpers (config-aware) ──────────────────────────────────

    public static void assertCanSendExternal(TypeCompte type) {
        if (!canSendExternal(type)) throw new BadRequestException(
                "Un compte épargne ne peut pas effectuer de virements externes. " +
                        "Transférez d'abord les fonds vers votre compte chèque ou professionnel, " +
                        "puis effectuez le virement depuis celui-ci.");
    }

    public static void assertMaxTransfer(TypeCompte type, double amount, FraudeConfig cfg) {
        double max = maxTransferAmount(type, cfg);
        if (amount > max) throw new BadRequestException(String.format(
                "Montant trop élevé pour un %s. Limite par virement : %.3f TND.", label(type), max));
    }

    public static void assertDailyAmount(TypeCompte type, double alreadySentToday,
                                         double newAmount, FraudeConfig cfg) {
        double limit = dailyAmountLimit(type, cfg);
        if (alreadySentToday + newAmount > limit) throw new BadRequestException(String.format(
                "Limite journalière de votre %s dépassée. Vous avez déjà envoyé %.3f TND aujourd'hui " +
                        "(limite : %.3f TND). Réessayez demain.",
                label(type), alreadySentToday, limit));
    }

    public static void assertDailyCount(TypeCompte type, long countToday, FraudeConfig cfg) {
        int limit = dailyCountLimit(type, cfg);
        if (countToday >= limit) throw new BadRequestException(String.format(
                "Vous avez atteint la limite de %d virements par jour pour votre %s. " +
                        "Votre compteur sera réinitialisé à minuit.", limit, label(type)));
    }

    public static void assertMonthlyCount(TypeCompte type, long countThisMonth, FraudeConfig cfg) {
        int limit = monthlyCountLimit(type, cfg);
        if (countThisMonth >= limit) throw new BadRequestException(String.format(
                "Vous avez atteint la limite de %d virements par mois pour votre %s.",
                limit, label(type)));
    }

    // ── Display helpers ─────────────────────────────────────────────────────

    public static String label(TypeCompte type) {
        return switch (type) {
            case COURANT       -> "compte chèque";
            case EPARGNE       -> "compte épargne";
            case PROFESSIONNEL -> "compte professionnel";
        };
    }

    public static String purpose(TypeCompte type) {
        return switch (type) {
            case COURANT       -> "Opérations quotidiennes (paiements, virements, retraits).";
            case EPARGNE       -> "Épargne rémunérée. Retraits limités pour favoriser l'accumulation.";
            case PROFESSIONNEL -> "Activité professionnelle à fort volume. Limites élevées.";
        };
    }
}