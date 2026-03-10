package com.sesame.neobte.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Compte implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompte;


    private Double solde;
    @Enumerated(EnumType.STRING)
    private TypeCompte typeCompte;
    @Enumerated(EnumType.STRING)
    private StatutCompte statutCompte;

    @ManyToOne
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "compteDe")
    @JsonIgnore
    private List<Virement> virementsSortants = new ArrayList<>();

    @OneToMany(mappedBy = "compteA")
    @JsonIgnore
    private List<Virement> virementsEntrants = new ArrayList<>();
}
