package com.sesame.neobte.Repositories.Investment;

import com.sesame.neobte.Entities.Class.Investment.MonthlyEarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IMonthlyEarningRepository extends JpaRepository<MonthlyEarning, Long> {

    List<MonthlyEarning> findByInvestment_IdOrderByMoisNumeroAsc(Long investmentId);

    Optional<MonthlyEarning> findByInvestment_IdAndMois(Long investmentId, String mois);

    @Query("SELECT COALESCE(SUM(e.montantInteret),0) FROM MonthlyEarning e " +
            "WHERE e.investment.id = :invId AND e.accrued = true")
    double totalAccruedByInvestment(@Param("invId") Long invId);
}
