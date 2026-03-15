package com.sesame.neobte.DTO.Responses.DemandeClotureCompte;

import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DemandeClotureResponseDTO {
    private Long id;
    private Long compteId;
    private String typeCompte;
    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String utilisateurEmail;
    private String motif;
    private StatutDemande statut;
    private String commentaireAdmin;
    private LocalDateTime dateDemande;
    private LocalDateTime dateDecision;
    private Double soldeAtDemande;
}
