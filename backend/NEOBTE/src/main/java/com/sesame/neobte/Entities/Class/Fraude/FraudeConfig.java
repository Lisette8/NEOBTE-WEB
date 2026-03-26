package com.sesame.neobte.Entities.Class.Fraude;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Convert;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudeConfig implements Serializable {

    @Id
    private Long id = 1L; // always one row

    // ── Fraud detection thresholds ──────────────────────────────────────────

    /** Single transfer above this triggers LARGE_SINGLE_TRANSFER alert */
    private double largeTransferThreshold = 5000.0;

    /** Rapid succession: X transfers within Y minutes triggers alert */
    private int rapidSuccessionCount = 3;
    private int rapidSuccessionMinutes = 5;

    /** Suspicious hour window — 24h clock */
    private int suspiciousHourStart = 2;
    private int suspiciousHourEnd   = 4;

    /** Send email to admin on every new fraud alert */
    @Convert(converter = BooleanToIntegerConverter.class)
    private boolean emailAlertsEnabled = true;

    // ── Per-account-type fee rates (editable by admin) ──────────────────────

    /** Checking account outgoing transfer fee (default 0.5%) */
    private double courantFeeRate        = 0.005;

    /** Savings account withdrawal fee — penalty to discourage (default 1.0%) */
    private double epargneFeeRate        = 0.010;

    /** Professional account fee — bulk discount (default 0.3%) */
    private double professionnelFeeRate  = 0.003;

    // ── Per-account-type daily limits (editable by admin) ───────────────────

    private double courantDailyAmountLimit       = 10_000.0;
    private int    courantDailyCountLimit         = 10;
    private int    courantMonthlyCountLimit       = 30;
    private double courantMaxTransfer             = 5_000.0;

    private double epargneDailyAmountLimit        = 2_000.0;
    private int    epargneDailyCountLimit          = 3;
    private int    epargneMonthlyCountLimit        = 5;
    private double epargneMaxTransfer              = 1_000.0;

    private double professionnelDailyAmountLimit  = 100_000.0;
    private int    professionnelDailyCountLimit    = 50;
    private int    professionnelMonthlyCountLimit  = 500;
    private double professionnelMaxTransfer        = 50_000.0;
}
