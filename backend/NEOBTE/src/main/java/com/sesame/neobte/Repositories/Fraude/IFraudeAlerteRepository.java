package com.sesame.neobte.Repositories.Fraude;

import com.sesame.neobte.Entities.Class.Fraude.FraudeAlerte;
import com.sesame.neobte.Entities.Class.Virement;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IFraudeAlerteRepository extends JpaRepository<FraudeAlerte, Long> {

    List<FraudeAlerte> findAllByOrderByDateAlerteDesc();

    List<FraudeAlerte> findByStatutOrderByDateAlerteDesc(FraudeStatut statut);

    List<FraudeAlerte> findByUtilisateur_IdUtilisateurOrderByDateAlerteDesc(Long userId);

    long countByStatut(FraudeStatut statut);

    /**
     * Count outgoing transfers by user since a given timestamp.
     * Used for both daily-count limit enforcement and rapid-succession detection.
     */
    @Query("SELECT COUNT(v) FROM Virement v " +
            "WHERE v.compteDe.utilisateur.idUtilisateur = :userId " +
            "AND v.dateDeVirement >= :since")
    long countTransfersSince(@Param("userId") Long userId, @Param("since") Date since);

    /**
     * Sum of amounts sent by user since a given timestamp.
     * Returns Number (not double) — Oracle's SUM returns BigDecimal, not double.
     */
    @Query("SELECT SUM(v.montant) FROM Virement v " +
            "WHERE v.compteDe.utilisateur.idUtilisateur = :userId " +
            "AND v.dateDeVirement >= :since")
    Number sumAmountSinceAsNumber(@Param("userId") Long userId, @Param("since") Date since);
}