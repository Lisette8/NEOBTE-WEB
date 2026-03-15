package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.FraisTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFraisTransactionRepository extends JpaRepository<FraisTransaction, Long> {
    List<FraisTransaction> findAllByOrderByDateCreationDesc();

    @Query("SELECT SUM(f.montantFrais) FROM FraisTransaction f")
    Double sumTotalFrais();
}
