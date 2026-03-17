package com.sesame.neobte.DTO.Responses.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponseDTO {

    // ── Key metrics ──────────────────────────────────────────
    private long totalTransfers;
    private double totalVolume;
    private double avgTransfer;
    private long totalClients;
    private long openFraudAlerts;

    // ── Time-series ──────────────────────────────────────────
    /** Last 30 days of actual daily transfer data */
    private List<DailyStatDTO> dailyTransfers;

    /** 7-day linear-regression forecast */
    private List<DailyStatDTO> forecast;

    /** Monthly new-user counts for the last 12 months */
    private List<MonthlyStatDTO> monthlyUsers;

    /** Fraud alerts grouped by type */
    private List<TypeStatDTO> fraudByType;

    /** Fraud alerts grouped by severity */
    private List<TypeStatDTO> fraudBySeverity;

    /** Daily fraud alert trend over last 30 days */
    private List<DailyStatDTO> fraudTrend;

    // ── Risk ─────────────────────────────────────────────────
    private List<UserRiskDTO> highRiskUsers;
}
