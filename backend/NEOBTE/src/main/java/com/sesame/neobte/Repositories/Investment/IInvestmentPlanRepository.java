package com.sesame.neobte.Repositories.Investment;

import com.sesame.neobte.Entities.Class.Investment.InvestmentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IInvestmentPlanRepository extends JpaRepository<InvestmentPlan, Long> {
    List<InvestmentPlan> findByActifTrueOrderByDureeEnMoisAsc();
}