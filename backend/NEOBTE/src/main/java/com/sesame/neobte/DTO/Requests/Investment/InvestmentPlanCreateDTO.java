package com.sesame.neobte.DTO.Requests.Investment;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentPlanCreateDTO {
    @NotBlank
    private String nom;
    private String description;
    @Min(1) @Max(360) private int dureeEnMois;
    @DecimalMin("0.001") @DecimalMax("1.0") private double tauxAnnuel;
    @Min(100) private double montantMin;
    @Min(100) private double montantMax;
    private boolean actif = true;
}