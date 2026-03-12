package com.sesame.neobte.Entities.Class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore // password is hashed — only visible in db
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date dateCreationCompte;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    // Tunisian national ID — required for banking (CIN)
    @Column(unique = true)
    private String cin;

    private String telephone;
    private String adresse;
    private String codePostal;
    private String pays;

    // Date of birth — replaces raw age, more standard in fintech
    private LocalDate dateNaissance;

    private String job;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Compte> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Support> supports = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<DemandeCompte> demandesCompte = new ArrayList<>();

}