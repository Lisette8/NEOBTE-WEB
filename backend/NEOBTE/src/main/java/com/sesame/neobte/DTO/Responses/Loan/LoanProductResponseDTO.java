package com.sesame.neobte.DTO.Responses.Loan;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class LoanProductResponseDTO {
    private Long id;
    private String nom;
    private String description;
    private String type;
    private int dureeEnMois;
    private double tauxAnnuel;
    private double montantMin;
    private double montantMax;
    private int gracePeriodDays;
    private double penaltyRate;
    private double penaltyFixedFee;
    private int defaultThreshold;
    private boolean actif;
    // Computed for UI simulator
    private double exampleMensualite; // for montantMin
}
