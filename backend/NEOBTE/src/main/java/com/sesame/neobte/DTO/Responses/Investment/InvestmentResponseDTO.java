package com.sesame.neobte.DTO.Responses.Investment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class InvestmentResponseDTO {
    private Long id;
    private Long compteId;
    private Long planId;
    private String planNom;
    private double montant;
    private double tauxAnnuel;
    private int dureeEnMois;
    private double interetAttendu;
    private double interetVerse;
    private LocalDateTime dateDebut;
    private LocalDateTime dateEcheance;
    private LocalDateTime dateCloture;
    private String statut;

    // Live progress
    private double progressPct;
    private double currentValue;
    private long daysRemaining;
    private double totalAccrued;  // sum of all accrued monthly earnings so far

    // Monthly breakdown (projected + accrued)
    private List<MonthlyEarningDTO> monthlyBreakdown;
}
