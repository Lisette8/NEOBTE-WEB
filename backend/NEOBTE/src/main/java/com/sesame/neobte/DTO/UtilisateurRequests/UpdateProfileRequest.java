package com.sesame.neobte.DTO.UtilisateurRequests;

import com.sesame.neobte.Entities.Genre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private Genre genre;
}
