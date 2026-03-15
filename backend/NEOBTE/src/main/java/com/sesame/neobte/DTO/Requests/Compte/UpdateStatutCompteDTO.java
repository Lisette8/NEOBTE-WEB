package com.sesame.neobte.DTO.Requests.Compte;

import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatutCompteDTO {
    @NotNull
    private StatutCompte newStatut;

    private String commentaire;
}
