package com.sesame.neobte.Entities;

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
public class Client implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idClient;

    @Column(unique = true)
    private String email;

    private String motDePasse;
    private Date dateCreationCompte;
    private String nom;
    private String prenom;
    private String adresse;
    private Integer age;
    private String job;
    private Genre genre;
    private Double solde;
}
