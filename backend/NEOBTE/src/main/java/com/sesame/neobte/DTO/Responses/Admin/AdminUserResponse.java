package com.sesame.neobte.DTO.Responses.Admin;

import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;


@Getter
@AllArgsConstructor
@Builder
public class AdminUserResponse {
    private Long id;
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
    private Role role;
    private Date dateCreationCompte;
    private Double totalSolde; //l'utilisateur peut avoir plusieurs comptes bancaires de plusieurs type, donc faut calculer la somme des soldes de chaque compte...
}
