package com.sesame.neobte.Repositories.Loan;

import com.sesame.neobte.Entities.Class.Loan.Loan;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUtilisateur_IdUtilisateurOrderByDateCreationDesc(Long userId);

    List<Loan> findByStatutOrderByDateCreationDesc(LoanStatut statut);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.utilisateur.idUtilisateur = :userId " +
            "AND l.statut IN ('ACTIVE','LATE','PENDING_APPROVAL','APPROVED')")
    long countActiveLoansByUser(@Param("userId") Long userId);

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.statut IN ('ACTIVE','LATE','DEFAULT')")
    long countActiveLoans();

    @Query("SELECT COALESCE(SUM(l.montant), 0) FROM Loan l WHERE l.statut IN ('ACTIVE','LATE','DEFAULT')")
    double totalOutstanding();

    @Query("SELECT COALESCE(SUM(l.totalRembourse), 0) FROM Loan l")
    double totalRepaid();

    @Query("SELECT COALESCE(SUM(l.totalPenalites), 0) FROM Loan l")
    double totalPenalties();
}