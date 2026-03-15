package com.sesame.neobte.DTO.Responses.Virement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VirementResponseDTO {
    private Long idVirement;
    private Long compteSourceId;
    private Long compteDestinationId;
    private String recipientName;
    private String senderName;
    private Double montant;

    //tax
    private Double frais;
    private Double totalDebite; //montant + frais — le paiement de la personne qui a envoyé
    private Double tauxFrais;

    private Date dateDeVirement;
    private String idempotencyKey;
}
