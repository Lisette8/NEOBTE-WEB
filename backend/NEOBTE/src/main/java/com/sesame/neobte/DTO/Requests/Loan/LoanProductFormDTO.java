package com.sesame.neobte.DTO.Requests.Loan;


import com.sesame.neobte.Entities.Enumeration.Loan.LoanType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class LoanProductFormDTO {
    @NotBlank private String nom;
    private String description;
    @NotNull private LoanType type;
    @Min(1) @Max(360) private int dureeEnMois;
    @DecimalMin("0.001") @DecimalMax("1.0") private double tauxAnnuel;
    @Min(100) private double montantMin;
    @Min(100) private double montantMax;
    @Min(0) @Max(30) private int gracePeriodDays;
    @DecimalMin("0.0") @DecimalMax("0.5") private double penaltyRate;
    @Min(0) private double penaltyFixedFee;
    @Min(1) @Max(12) private int defaultThreshold;
    private boolean actif;
}
