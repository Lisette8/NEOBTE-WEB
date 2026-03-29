package com.sesame.neobte.DTO.Requests.Loan;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoanRequestDTO {
    @NotNull private Long productId;
    @NotNull private Long compteDestinationId;
    @NotNull private Long comptePrelevementId;
    @NotNull @Min(100) private Double montant;
    private String motif; // optional — client explains purpose
}