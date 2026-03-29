package com.sesame.neobte.Entities.Class.Loan;

import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanStatut;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanType;
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
public class Loan implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Compte compteDestination; // account where funds are disbursed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Compte comptePrelevement; // account from which monthly payments are deducted

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private LoanProduct product;

    // Snapshot fields locked at approval time
    @Column(nullable = false) private String productNom;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private LoanType type;
    @Column(nullable = false) private double montant;          // principal
    @Column(nullable = false) private double tauxAnnuel;       // locked rate
    @Column(nullable = false) private int    dureeEnMois;
    @Column(nullable = false) private double mensualite;       // fixed monthly payment (annuity)
    @Column(nullable = false) private double totalDu;          // montant + total interest
    @Column(nullable = false) private double totalInteret;     // totalDu - montant

    // Running totals
    private double totalRembourse   = 0.0;
    private double totalPenalites   = 0.0;
    private int    missedPayments   = 0;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatut statut = LoanStatut.PENDING_APPROVAL;

    private String motifRejet;
    private String adminNote;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateApprobation;
    private LocalDateTime dateDisbursement;
    private LocalDateTime dateCloture;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean fundsReleased = false; // true once money credited to client account

    @PrePersist
    protected void onCreate() { this.dateCreation = LocalDateTime.now(); }
}
