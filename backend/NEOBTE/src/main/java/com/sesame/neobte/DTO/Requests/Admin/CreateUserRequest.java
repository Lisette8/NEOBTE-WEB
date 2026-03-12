package com.sesame.neobte.DTO.Requests.Admin;

import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class CreateUserRequest {
    private String email;
    private String username;
    private String nom;
    private String prenom;
    private String cin;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String pays;
    private LocalDate dateNaissance;
    private String job;
    private Genre genre;
    private String motDePasse;
    private Role role;
}
