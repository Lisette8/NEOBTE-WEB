package com.sesame.neobte.DTO.Responses.Admin;

import com.sesame.neobte.Entities.Enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class AdminUserResponse {
    private Long id;
    private String email;
    private String nom;
    private String prenom;
    private Integer age;
    private String adresse;
    private String job;
    private String genre;
    private Double totalSolde; //l'utilisateur peut avoir plusieurs comptes bancaires de plusieurs type, donc faut calculer la somme des soldes de chaque compte...
    private Role role;
}
