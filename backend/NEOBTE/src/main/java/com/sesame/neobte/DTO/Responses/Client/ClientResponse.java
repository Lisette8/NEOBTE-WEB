package com.sesame.neobte.DTO.Responses.Client;

import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientResponse {
    private Long id;
    private String email;
    private String username;
    private String nom;
    private String prenom;
    private String photoUrl;
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

}
