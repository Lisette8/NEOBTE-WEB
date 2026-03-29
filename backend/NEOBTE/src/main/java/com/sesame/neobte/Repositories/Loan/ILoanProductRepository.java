package com.sesame.neobte.Repositories.Loan;

import com.sesame.neobte.Entities.Class.Loan.LoanProduct;
import com.sesame.neobte.Entities.Enumeration.Loan.LoanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ILoanProductRepository extends JpaRepository<LoanProduct, Long> {
    List<LoanProduct> findByActifTrueOrderByDureeEnMoisAsc();
    List<LoanProduct> findByTypeAndActifTrue(LoanType type);
}
