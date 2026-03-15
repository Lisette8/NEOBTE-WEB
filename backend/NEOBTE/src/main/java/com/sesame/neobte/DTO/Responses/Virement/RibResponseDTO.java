package com.sesame.neobte.DTO.Responses.Virement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RibResponseDTO {
    private String nomComplet;
    private String email;
    private String telephone;

    private List<RibCompteDTO> comptes;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RibCompteDTO {
        private Long idCompte;
        private String typeCompte;
        private String rib;
        private Double solde;
        private String statutCompte;
        private boolean isPrimary;
    }
}
