package com.sesame.neobte.DTO.Responses.Virement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirementResponseDTO {
    private Long idVirement;

    @NotNull
    private Long compteSourceId;

    @NotNull
    private Long compteDestinationId;

    @NotNull
    @Min(1)
    private Double montant;

    private Date dateDeVirement;

    private String idempotencyKey;
}
