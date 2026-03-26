package com.sesame.neobte.Repositories.Investment;

import com.sesame.neobte.Entities.Class.Investment.Investment;
import com.sesame.neobte.Entities.Enumeration.Investment.InvestmentStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IInvestmentRepository extends JpaRepository<Investment, Long> {

    List<Investment> findByUtilisateur_IdUtilisateurOrderByDateDebutDesc(Long userId);
    List<Investment> findByStatutOrderByDateDebutDesc(InvestmentStatut statut);

    @Query("SELECT i FROM Investment i WHERE i.statut = 'ACTIVE' AND i.dateEcheance <= :now")
    List<Investment> findMatured(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.statut = :statut")
    long countByStatut(@Param("statut") InvestmentStatut statut);

    @Query("SELECT COUNT(i) FROM Investment i WHERE i.utilisateur.idUtilisateur = :userId AND i.statut = 'ACTIVE'")
    long countActiveByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(i.montant), 0) FROM Investment i WHERE i.utilisateur.idUtilisateur = :userId AND i.statut = 'ACTIVE'")
    Double totalLockedByUser(@Param("userId") Long userId);

    /** Total interest paid out to clients across all completed investments */
    @Query("SELECT COALESCE(SUM(i.interetVerse), 0) FROM Investment i WHERE i.statut = 'COMPLETED'")
    double totalInterestPaid();
}
