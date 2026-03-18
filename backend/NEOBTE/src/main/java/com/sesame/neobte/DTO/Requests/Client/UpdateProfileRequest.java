package com.sesame.neobte.DTO.Requests.Client;

import com.sesame.neobte.Entities.Enumeration.Genre;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Pattern(regexp = "^$|^[+]?[0-9]{8,15}$", message = "Numéro de téléphone invalide")
    private String telephone;

    private String job;

    private Genre genre;
    private String adresse;
    private String codePostal;
    private String pays;
}
