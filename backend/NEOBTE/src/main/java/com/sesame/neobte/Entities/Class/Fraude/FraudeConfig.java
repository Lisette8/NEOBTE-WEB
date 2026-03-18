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

    /** Max number of transfers per user per 24h before DAILY_COUNT alert */
    private int dailyCountLimit = 10;

    /** Max total amount (TND) sent per user per 24h before DAILY_AMOUNT alert */
    private double dailyAmountLimit = 5000.0;

    /** Single transfer above this amount triggers LARGE_SINGLE_TRANSFER alert */
    private double largeTransferThreshold = 2000.0;

    /** Rapid succession: X transfers within Y minutes triggers alert */
    private int rapidSuccessionCount = 3;
    private int rapidSuccessionMinutes = 5;

    /** Suspicious hour window — 24h clock */
    private int suspiciousHourStart = 2;  // 02:00
    private int suspiciousHourEnd   = 4;  // 04:00

    /** Send email to admin on every new alert */
    @Convert(converter = BooleanToIntegerConverter.class)
    private boolean emailAlertsEnabled = true;
}
