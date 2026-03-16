package com.sesame.neobte.DTO.Requests.Fraude;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FraudeConfigUpdateDTO {
    @Min(1)
    private int dailyCountLimit;

    @Min(100)
    private double dailyAmountLimit;

    @Min(100)
    private double largeTransferThreshold;

    @Min(2)
    @Max(20)
    private int rapidSuccessionCount;

    @Min(1)
    @Max(60)
    private int rapidSuccessionMinutes;

    @Min(0)
    @Max(23)
    private int suspiciousHourStart;

    @Min(0)
    @Max(23)
    private int suspiciousHourEnd;

    private boolean emailAlertsEnabled;
}