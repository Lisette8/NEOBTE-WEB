package com.sesame.neobte.Entities.Class.Loan;

import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import com.sesame.neobte.Entities.Enumeration.Loan.RepaymentStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRepayment implements Serializable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Loan loan;

    /** Installment number: 1, 2, … dureeEnMois */
    @Column(nullable = false)
    private int installmentNumber;

    /** The date this installment is due */
    @Column(nullable = false)
    private LocalDate dateDue;

    /** Fixed monthly payment amount (principal + interest portion) */
    @Column(nullable = false)
    private double montantDu;

    /** Principal portion of this installment */
    @Column(nullable = false)
    private double principalPortion;

    /** Interest portion of this installment */
    @Column(nullable = false)
    private double interetPortion;

    /** Penalty applied if this installment was late */
    private double penalite = 0.0;

    /** Total actually collected (montantDu + penalite if late) */
    private double montantPaye = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatut statut = RepaymentStatut.PENDING;

    private LocalDateTime datePaiement; // when it was collected

    /** How many collection attempts were made */
    private int retryCount = 0;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean penaltyApplied = false;
}
