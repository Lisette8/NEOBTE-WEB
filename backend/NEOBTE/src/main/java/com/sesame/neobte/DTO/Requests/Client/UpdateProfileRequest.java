package com.sesame.neobte.DTO.Requests.Client;

import com.sesame.neobte.Entities.Genre;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Nom is required")
    private String nom;

    @NotBlank(message = "Prenom is required")
    private String prenom;

    private String adresse;

    @NotNull(message = "Age is required")
    @Min(18)
    private Integer age;

    @NotBlank(message = "Job is required")
    private String job;
    private Genre genre;
}
