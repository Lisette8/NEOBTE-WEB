package com.sesame.neobte.DTO.Requests.Investment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentCreateDTO {
    @NotNull
    private Long planId;
    @NotNull private Long compteId;
    @NotNull @Min(1) private Double montant;
}
