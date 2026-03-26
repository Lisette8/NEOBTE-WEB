package com.sesame.neobte.Entities.Class.Investment;

import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Investment.InvestmentStatut;
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
public class Investment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Compte compte; // source account — funds deducted from here

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private InvestmentPlan plan;

    // Snapshot values locked at subscription time
    @Column(nullable = false)
    private String planNom;

    @Column(nullable = false)
    private double montant;          // principal invested

    @Column(nullable = false)
    private double tauxAnnuel;       // locked at creation

    @Column(nullable = false)
    private int dureeEnMois;         // locked at creation

    @Column(nullable = false)
    private double interetAttendu;   // pre-calculated profit = montant * rate * (months/12)

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateEcheance; // dateDebut + dureeEnMois

    private LocalDateTime dateCloture;  // set when COMPLETED or CANCELLED

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestmentStatut statut = InvestmentStatut.ACTIVE;

    // Actual profit paid out (= interetAttendu if completed, 0 if cancelled)
    private double interetVerse = 0.0;
}
