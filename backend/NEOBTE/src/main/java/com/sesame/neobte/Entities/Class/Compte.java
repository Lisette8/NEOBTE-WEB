package com.sesame.neobte.Entities.Class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
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

    // Used to determine primary account — earliest created COURANT, or overall earliest
    @Column(nullable = false, updatable = false)
    private Date dateCreation;

    // Set when client submits closure request — auto-deleted after 48h if not cancelled
    private java.time.LocalDateTime dateSuppressionPrevue;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = new Date();
    }

    @ManyToOne
    private Utilisateur utilisateur;

    @OneToMany(mappedBy = "compteDe")
    @JsonIgnore
    private List<Virement> virementsSortants = new ArrayList<>();

    @OneToMany(mappedBy = "compteA")
    @JsonIgnore
    private List<Virement> virementsEntrants = new ArrayList<>();
}