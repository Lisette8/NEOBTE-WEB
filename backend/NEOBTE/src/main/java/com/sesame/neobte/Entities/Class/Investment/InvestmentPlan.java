package com.sesame.neobte.Entities.Class.Investment;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentPlan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name, e.g. "Court terme — 3 mois" */
    @Column(nullable = false)
    private String nom;

    /** Short description shown in the UI */
    @Column(length = 500)
    private String description;

    /** Duration in months */
    @Column(nullable = false)
    private int dureeEnMois;

    /** Annual interest rate, e.g. 0.06 = 6% */
    @Column(nullable = false)
    private double tauxAnnuel;

    /** Minimum investment amount (TND) */
    @Column(nullable = false)
    private double montantMin;

    /** Maximum investment amount per subscription (TND) */
    @Column(nullable = false)
    private double montantMax;

    /** Whether new subscriptions are allowed */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean actif = true;
}
