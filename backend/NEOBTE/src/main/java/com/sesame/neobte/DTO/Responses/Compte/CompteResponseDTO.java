package com.sesame.neobte.DTO.Responses.Compte;

import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompteResponseDTO {
    private Long idCompte;
    private Double solde;
    private TypeCompte typeCompte;
    private StatutCompte statutCompte;
    private Long utilisateurId;
    private LocalDateTime dateSuppressionPrevue;
}
