package com.sesame.neobte.DTO.Requests.Virement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirementCreateDTO {
    @NotNull
    private Long compteSourceId;

    @NotNull
    private Long compteDestinationId;

    @NotNull
    @Min(1)
    private Double montant;

    private String idempotencyKey;
}
