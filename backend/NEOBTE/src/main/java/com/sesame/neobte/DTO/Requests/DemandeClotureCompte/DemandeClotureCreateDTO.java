package com.sesame.neobte.DTO.Requests.DemandeClotureCompte;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DemandeClotureCreateDTO {
    @NotNull
    private Long compteId;

    @NotBlank(message = "Un motif est requis")
    @Size(max = 500)
    private String motif;
}
