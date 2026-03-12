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

    @NotBlank(message = "Last name is required")
    private String nom;

    @NotBlank(message = "First name is required")
    private String prenom;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Invalid phone number")
    private String telephone;

    @NotBlank(message = "Job is required")
    private String job;

    private Genre genre;
    private String adresse;
    private String codePostal;
    private String pays;
}
