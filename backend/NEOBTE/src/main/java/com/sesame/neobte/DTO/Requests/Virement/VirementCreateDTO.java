package com.sesame.neobte.DTO.Requests.Virement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirementCreateDTO {
    @NotBlank(message = "Recipient identifier (email or phone) is required")
    private String recipientIdentifier;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1 TND")
    private Double montant;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
