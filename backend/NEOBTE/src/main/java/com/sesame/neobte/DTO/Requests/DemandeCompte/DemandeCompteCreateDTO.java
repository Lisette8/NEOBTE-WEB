package com.sesame.neobte.DTO.Requests.DemandeCompte;

import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemandeCompteCreateDTO {
    @NotNull(message = "Account type is required")
    private TypeCompte typeCompte;

    @Size(max = 500, message = "Motif cannot exceed 500 characters")
    private String motif;

    @NotNull(message = "User ID is required")
    private Long utilisateurId;
}
