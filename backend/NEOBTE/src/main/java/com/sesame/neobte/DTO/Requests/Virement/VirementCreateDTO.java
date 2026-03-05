package com.sesame.neobte.DTO.Requests.Virement;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VirementCreateDTO {
    private Long compteSourceId;
    private Long compteDestinationId;
    private Double montant;
}
