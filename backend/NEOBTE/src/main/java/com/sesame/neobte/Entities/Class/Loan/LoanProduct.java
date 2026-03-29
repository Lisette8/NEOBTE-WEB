package com.sesame.neobte.Entities.Class.Loan;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * Admin-defined loan product template.
 * Interest rate is annual nominal rate (taux annuel effectif global — TAEG in Tunisian banking).
 * Monthly payment uses reducing-balance (annuity) formula: standard bank practice.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanProduct implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(length = 600)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType type;

    /** Duration in months */
    @Column(nullable = false)
    private int dureeEnMois;

    /** Annual interest rate e.g. 0.12 = 12% */
    @Column(nullable = false)
    private double tauxAnnuel;

    @Column(nullable = false)
    private double montantMin;

    @Column(nullable = false)
    private double montantMax;

    /** Grace period in days before penalty is applied after a missed payment */
    @Column(nullable = false)
    private int gracePeriodDays = 2;

    /** Penalty as a % of the missed installment */
    @Column(nullable = false)
    private double penaltyRate = 0.05;  // 5%

    /** Fixed penalty fee per missed installment (TND) */
    @Column(nullable = false)
    private double penaltyFixedFee = 15.0;

    /** Number of missed payments before loan is marked DEFAULT */
    @Column(nullable = false)
    private int defaultThreshold = 3;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean actif = true;
}