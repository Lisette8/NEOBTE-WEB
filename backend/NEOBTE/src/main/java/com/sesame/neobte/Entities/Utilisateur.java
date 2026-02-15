package com.sesame.neobte.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;

    @Column(unique = true)
    private String email;

    @JsonIgnore //password is hashed and hidden. hash is only visible in the db
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date dateCreationCompte;
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private Genre genre;
    private Double solde;

}
