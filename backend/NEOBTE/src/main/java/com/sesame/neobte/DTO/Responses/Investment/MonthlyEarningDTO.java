package com.sesame.neobte.DTO.Responses.Investment;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonthlyEarningDTO {
    private int moisNumero;
    private String mois;          // "2025-03"
    private String moisLabel;     // "Mars 2025"
    private double montantInteret;
    private boolean accrued;
}
