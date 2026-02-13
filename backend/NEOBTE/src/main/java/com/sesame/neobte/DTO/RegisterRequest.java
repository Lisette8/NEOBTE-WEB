package com.sesame.neobte.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private String genre;
    private String motDePasse;
}
