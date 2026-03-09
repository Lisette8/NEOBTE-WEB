package com.sesame.neobte.DTO.Requests.Compte;


import com.sesame.neobte.Entities.TypeCompte;
import lombok.Data;

@Data
public class CompteCreateDTO {
    private Double solde;
    private TypeCompte typeCompte;
    private Long utilisateurId;
}
