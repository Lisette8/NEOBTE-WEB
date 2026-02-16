package com.sesame.neobte.DTO.Requests.Admin;

import com.sesame.neobte.Entities.Genre;
import com.sesame.neobte.Entities.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateUserRequest {
    private String email;
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private Genre genre;
    private String motDePasse;
    private Role role;
    private Double solde;
}
