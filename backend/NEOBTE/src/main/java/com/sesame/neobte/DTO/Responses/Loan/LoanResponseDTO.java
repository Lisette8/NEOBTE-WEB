package com.sesame.neobte.DTO.Responses.Loan;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class LoanResponseDTO {
    private Long id;
    private Long compteDestinationId;
    private Long comptePrelevementId;
    private Long productId;
    private String productNom;
    private String type;
    private double montant;
    private double tauxAnnuel;
    private int dureeEnMois;
    private double mensualite;
    private double totalDu;
    private double totalInteret;
    private double totalRembourse;
    private double totalPenalites;
    private double resteADu;       // totalDu - totalRembourse
    private int    missedPayments;
    private String statut;
    private String motifRejet;
    private String adminNote;
    private LocalDateTime dateCreation;
    private LocalDateTime dateApprobation;
    private LocalDateTime dateDisbursement;
    private LocalDateTime dateCloture;
    // Progress
    private double progressPct;
    private List<LoanRepaymentResponseDTO> repayments;
}
