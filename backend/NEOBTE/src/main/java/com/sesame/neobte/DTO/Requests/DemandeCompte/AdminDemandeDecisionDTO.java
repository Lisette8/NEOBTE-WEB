package com.sesame.neobte.DTO.Requests.DemandeCompte;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminDemandeDecisionDTO {
    @Size(max = 500, message = "Comment cannot exceed 500 characters")
    private String commentaireAdmin;
}
