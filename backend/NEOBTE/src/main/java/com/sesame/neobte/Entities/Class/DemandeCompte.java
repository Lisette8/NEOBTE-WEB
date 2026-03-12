package com.sesame.neobte.Entities.Class;

import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeCompte implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDemande;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeCompte typeCompte;

    @Column(length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutDemande statutDemande = StatutDemande.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime dateDemande;

    private LocalDateTime dateDecision;


    //si demande de creation de compte rejetee
    @Column(length = 500)
    private String commentaireAdmin;

    //si demande accepte -> creation de compte
    @OneToOne
    @JoinColumn(name = "compte_id")
    private Compte compteOuvert;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;



    @PrePersist
    public void prePersist() {
        this.dateDemande = LocalDateTime.now();
        if (this.statutDemande == null) {
            this.statutDemande = StatutDemande.EN_ATTENTE;
        }
    }
}
