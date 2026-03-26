package com.sesame.neobte.DTO.Responses.Fraude;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class FraudeConfigResponseDTO {
    // Fraud detection
    private double largeTransferThreshold;
    private int    rapidSuccessionCount;
    private int    rapidSuccessionMinutes;
    private int    suspiciousHourStart;
    private int    suspiciousHourEnd;
    private boolean emailAlertsEnabled;

    // Per-type fee rates
    private double courantFeeRate;
    private double epargneFeeRate;
    private double professionnelFeeRate;

    // Per-type limits — COURANT
    private double courantDailyAmountLimit;
    private int    courantDailyCountLimit;
    private int    courantMonthlyCountLimit;
    private double courantMaxTransfer;

    // Per-type limits — EPARGNE
    private double epargneDailyAmountLimit;
    private int    epargneDailyCountLimit;
    private int    epargneMonthlyCountLimit;
    private double epargneMaxTransfer;

    // Per-type limits — PROFESSIONNEL
    private double professionnelDailyAmountLimit;
    private int    professionnelDailyCountLimit;
    private int    professionnelMonthlyCountLimit;
    private double professionnelMaxTransfer;
}
