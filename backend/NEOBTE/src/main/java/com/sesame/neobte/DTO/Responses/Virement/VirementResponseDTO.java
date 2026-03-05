package com.sesame.neobte.DTO.Responses.Virement;

import lombok.*;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirementResponseDTO {
    private Long idVirement;
    private Long compteSourceId;
    private Long compteDestinationId;
    private Double montant;
    private Date dateDeVirement;
}
