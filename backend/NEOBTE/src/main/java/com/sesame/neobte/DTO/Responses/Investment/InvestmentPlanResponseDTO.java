package com.sesame.neobte.DTO.Responses.Investment;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvestmentPlanResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private int dureeEnMois;
    private double tauxAnnuel;
    private double montantMin;
    private double montantMax;
    private boolean actif;
}