package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface IVirementRepository extends JpaRepository<Virement, Long> {

    List<Virement> findByCompteDeIdCompte(Long compteId);
    List<Virement> findByCompteAIdCompte(Long compteId);
    List<Virement> findByCompteDeIdCompteOrCompteAIdCompte(Long sourceId, Long destinationId);

    Optional<Virement> findByIdempotencyKey(String idempotencyKey);

    /** Daily transfer stats (date, total_amount, count) over a rolling window — Oracle TRUNC */
    @Query(value = "SELECT TRUNC(date_de_virement) AS day, " +
            "SUM(montant) AS total_amount, COUNT(*) AS cnt " +
            "FROM virement " +
            "WHERE date_de_virement >= :since " +
            "GROUP BY TRUNC(date_de_virement) " +
            "ORDER BY TRUNC(date_de_virement)", nativeQuery = true)
    List<Object[]> dailyTransferStats(@Param("since") Date since);

    /** Count outgoing transfers made by a user since a given date (used for monthly plan limits). */
    @Query("SELECT COUNT(v) FROM Virement v WHERE v.compteDe.utilisateur.idUtilisateur = :userId AND v.dateDeVirement >= :since")
    long countOutgoingSince(@Param("userId") Long userId, @Param("since") Date since);

    @Query("SELECT COUNT(v) FROM Virement v")
    long countTotal();

    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM Virement v")
    Double totalVolume();

    @Query("SELECT COALESCE(AVG(v.montant), 0) FROM Virement v")
    Double avgTransfer();
}
