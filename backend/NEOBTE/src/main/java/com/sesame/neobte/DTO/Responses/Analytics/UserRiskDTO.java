package com.sesame.neobte.DTO.Responses.Analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRiskDTO {
    private Long userId;
    private String nom;
    private String email;
    private long alertCount;
    private long highSeverityCount;
    /** HIGH / MEDIUM / LOW — computed server-side */
    private String riskLevel;
}
