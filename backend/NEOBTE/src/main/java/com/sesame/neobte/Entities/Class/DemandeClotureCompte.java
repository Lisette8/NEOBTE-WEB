package com.sesame.neobte.Entities.Class;

import com.sesame.neobte.Entities.Enumeration.StatutDemande;
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
@AllArgsConstructor
@NoArgsConstructor
public class DemandeClotureCompte implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Compte compte;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 500)
    private String motif;

    @Enumerated(EnumType.STRING)
    private StatutDemande statut = StatutDemande.EN_ATTENTE;

    private String commentaireAdmin;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateDemande;

    private LocalDateTime dateDecision;

    @PrePersist
    protected void onCreate() { this.dateDemande = LocalDateTime.now(); }
}
