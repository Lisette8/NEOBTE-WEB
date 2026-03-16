package com.sesame.neobte.DTO.Responses.Fraude;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudeConfigResponseDTO {
    private int dailyCountLimit;
    private double dailyAmountLimit;
    private double largeTransferThreshold;
    private int rapidSuccessionCount;
    private int rapidSuccessionMinutes;
    private int suspiciousHourStart;
    private int suspiciousHourEnd;
    private boolean emailAlertsEnabled;
}