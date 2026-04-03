package com.sesame.neobte.DTO.Requests.Support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SupportCreateDTO {
    private String sujet;
    private String message;

    /**
     * Category of the issue.
     * Accepted values: VIREMENT, COMPTE, CARTE, PRET, PLACEMENT, SECURITE, AUTRE
     * Defaults to AUTRE if null or invalid.
     */
    private String categorie;

    /**
     * Priority of the ticket.
     * Accepted values: NORMALE, URGENTE
     * Defaults to NORMALE if null or invalid.
     */
    private String priorite;
}

