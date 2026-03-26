package com.sesame.neobte.Entities.Class.Investment;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MonthlyEarning implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Investment investment;

    /** e.g. "2025-03" */
    @Column(nullable = false)
    private String mois;

    /** Month number within the plan: 1, 2, 3… */
    @Column(nullable = false)
    private int moisNumero;

    /** Interest earned this month = principal × annualRate / 12 */
    @Column(nullable = false)
    private double montantInteret;

    /**
     * Whether this month's interest has been accrued (recorded as earned).
     * Uses NUMBER(1) — Oracle 21c does not support SQL BOOLEAN.
     */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean accrued = false;
}