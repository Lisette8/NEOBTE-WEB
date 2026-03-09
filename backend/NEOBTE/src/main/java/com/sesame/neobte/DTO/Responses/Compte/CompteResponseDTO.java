package com.sesame.neobte.DTO.Responses.Compte;

import com.sesame.neobte.Entities.StatutCompte;
import com.sesame.neobte.Entities.TypeCompte;
import lombok.Data;

@Data
public class CompteResponseDTO {
    private Long idCompte;
    private Double solde;
    private TypeCompte typeCompte;
    private StatutCompte statutCompte;
    private Long utilisateurId;
}
