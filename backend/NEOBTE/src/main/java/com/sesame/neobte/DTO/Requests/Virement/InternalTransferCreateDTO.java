package com.sesame.neobte.DTO.Requests.Virement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InternalTransferCreateDTO {

    @NotNull(message = "Le compte source est obligatoire")
    private Long compteSourceId;

    @NotNull(message = "Le compte destination est obligatoire")
    private Long compteDestinationId;

    @NotNull(message = "Le montant est obligatoire")
    @Min(value = 1, message = "Le montant doit être au moins 1 TND")
    private Double montant;

    @NotNull(message = "La clé d'idempotence est obligatoire")
    private String idempotencyKey;
}
