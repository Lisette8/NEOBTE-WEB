package com.sesame.neobte.DTO.Requests.Fraude;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class FraudeConfigUpdateDTO {
    // Fraud detection
    @Min(100) private double largeTransferThreshold;
    @Min(2) @Max(20) private int rapidSuccessionCount;
    @Min(1) @Max(60) private int rapidSuccessionMinutes;
    @Min(0) @Max(23) private int suspiciousHourStart;
    @Min(0) @Max(23) private int suspiciousHourEnd;
    private boolean emailAlertsEnabled;

    // Fee rates
    @DecimalMin("0.0") @DecimalMax("0.10") private double courantFeeRate;
    @DecimalMin("0.0") @DecimalMax("0.10") private double epargneFeeRate;
    @DecimalMin("0.0") @DecimalMax("0.10") private double professionnelFeeRate;

    // COURANT limits
    @Min(100) private double courantDailyAmountLimit;
    @Min(1)   private int    courantDailyCountLimit;
    @Min(1)   private int    courantMonthlyCountLimit;
    @Min(100) private double courantMaxTransfer;

    // EPARGNE limits
    @Min(100) private double epargneDailyAmountLimit;
    @Min(1)   private int    epargneDailyCountLimit;
    @Min(1)   private int    epargneMonthlyCountLimit;
    @Min(100) private double epargneMaxTransfer;

    // PROFESSIONNEL limits
    @Min(100) private double professionnelDailyAmountLimit;
    @Min(1)   private int    professionnelDailyCountLimit;
    @Min(1)   private int    professionnelMonthlyCountLimit;
    @Min(100) private double professionnelMaxTransfer;
}