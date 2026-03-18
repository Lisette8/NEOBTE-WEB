package com.sesame.neobte.Entities.Class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @JsonIgnore // password is hashed — hash visible in db
    private String motDePasse;

    /** Last successful password change timestamp (rate-limiting, security). */
    private LocalDateTime dateDernierChangementMotDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date dateCreationCompte;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true)
    private String cin;

    @Column(unique = true)
    private String telephone;

    private String adresse;
    private String codePostal;
    private String pays;
    private LocalDate dateNaissance;
    private String job;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(length = 500)
    private String photoUrl;

    /**
     * Oracle 21c doesn't support BOOLEAN as a table column type.
     * Let Hibernate map this to NUMBER(1) with the usual 0/1 check.
     */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean premium = false;

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
