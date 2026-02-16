package com.sesame.neobte.DTO.Responses.Admin;

import com.sesame.neobte.Entities.Role;
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
    private Double solde;
    private Role role;
}
