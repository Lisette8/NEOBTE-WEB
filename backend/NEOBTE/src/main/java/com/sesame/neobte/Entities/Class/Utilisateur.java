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

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Compte> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Support> supports = new ArrayList<>();

}
