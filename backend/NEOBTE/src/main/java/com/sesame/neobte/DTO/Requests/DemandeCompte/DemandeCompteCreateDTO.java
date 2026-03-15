package com.sesame.neobte.DTO.Requests.DemandeCompte;

import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DemandeCompteCreateDTO {
    @NotNull(message = "Account type is required")
    private TypeCompte typeCompte;

    @Size(max = 500)
    private String motif;

    @NotBlank(message = "CIN is required")
    @Pattern(regexp = "^[0-9]{8}$", message = "CIN must be exactly 8 digits")
    private String cin;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateNaissance;

    // obligatoire pour compte COURANT et PROFESSIONNEL
    private String adresse;
    private String job;

    // obligatoire seulement pour compte PROFESSIONNEL
    private String nomEntreprise;
}
