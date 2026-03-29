package com.sesame.neobte.Repositories.Loan;

import com.sesame.neobte.Entities.Class.Loan.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ILoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {

    List<LoanRepayment> findByLoan_IdOrderByInstallmentNumberAsc(Long loanId);

    /** Repayments due today or earlier that are still pending */
    @Query("SELECT r FROM LoanRepayment r WHERE r.statut = 'PENDING' AND r.dateDue <= :today")
    List<LoanRepayment> findDueRepayments(@Param("today") LocalDate today);

    /** Repayments past grace period (due before cutoff) that haven't had penalty applied */
    @Query("SELECT r FROM LoanRepayment r WHERE r.statut IN ('PENDING','LATE') " +
            "AND r.dateDue < :graceCutoff AND r.penaltyApplied = false")
    List<LoanRepayment> findPastGracePeriod(@Param("graceCutoff") LocalDate graceCutoff);

    @Query("SELECT COUNT(r) FROM LoanRepayment r WHERE r.loan.id = :loanId " +
            "AND r.statut IN ('LATE','FAILED')")
    long countMissedByLoan(@Param("loanId") Long loanId);
}
