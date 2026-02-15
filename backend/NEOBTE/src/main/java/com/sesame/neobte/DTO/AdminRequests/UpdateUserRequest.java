package com.sesame.neobte.DTO.AdminRequests;

import com.sesame.neobte.Entities.Genre;
import com.sesame.neobte.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateUserRequest {
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private Genre genre;
    private Double solde;
    private String motDePasse;
    private Role role;
}
