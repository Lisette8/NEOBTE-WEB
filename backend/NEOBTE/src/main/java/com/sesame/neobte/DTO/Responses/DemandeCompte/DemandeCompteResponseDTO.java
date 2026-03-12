package com.sesame.neobte.DTO.Responses.DemandeCompte;

import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DemandeCompteResponseDTO {
    private Long idDemande;
    private TypeCompte typeCompte;
    private String motif;
    private StatutDemande statutDemande;
    private LocalDateTime dateDemande;
    private LocalDateTime dateDecision;
    private String commentaireAdmin;
    private Long compteOuvertId;

    //pour admin
    private Long utilisateurId;
    private String utilisateurUsername;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String utilisateurTelephone;
    private String utilisateurCin;
}
