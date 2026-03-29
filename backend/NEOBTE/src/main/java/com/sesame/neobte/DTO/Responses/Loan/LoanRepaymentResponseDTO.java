package com.sesame.neobte.DTO.Responses.Loan;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class LoanRepaymentResponseDTO {
    private Long id;
    private int installmentNumber;
    private LocalDate dateDue;
    private double montantDu;
    private double principalPortion;
    private double interetPortion;
    private double penalite;
    private double montantPaye;
    private String statut;
    private LocalDateTime datePaiement;
    private boolean penaltyApplied;
}
