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
    void deleteByUtilisateur_IdUtilisateur(Long userId);

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

    // ── Analytics queries ──────────────────────────────────────────────────────

    @Query("SELECT a.type, COUNT(a) FROM FraudeAlerte a GROUP BY a.type")
    List<Object[]> countByType();

    @Query("SELECT a.severity, COUNT(a) FROM FraudeAlerte a GROUP BY a.severity")
    List<Object[]> countBySeverity();

    /** Daily fraud alert trend (Oracle TRUNC) */
    @Query(value = "SELECT TRUNC(date_alerte) AS day, COUNT(*) AS cnt " +
            "FROM fraude_alerte " +
            "WHERE date_alerte >= :since " +
            "GROUP BY TRUNC(date_alerte) " +
            "ORDER BY TRUNC(date_alerte)", nativeQuery = true)
    List<Object[]> dailyAlertTrend(@Param("since") Date since);

    /**
     * Per-user open alert summary for risk scoring.
     * Returns: [userId, prenom, nom, email, totalAlerts, highSeverityAlerts]
     */
    @Query("SELECT a.utilisateur.idUtilisateur, a.utilisateur.prenom, a.utilisateur.nom, " +
            "a.utilisateur.email, COUNT(a), " +
            "SUM(CASE WHEN a.severity = com.sesame.neobte.Entities.Enumeration.Fraude.FraudeSeverity.HIGH THEN 1 ELSE 0 END) " +
            "FROM FraudeAlerte a WHERE a.statut = com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut.OPEN " +
            "GROUP BY a.utilisateur.idUtilisateur, a.utilisateur.prenom, a.utilisateur.nom, a.utilisateur.email " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> userRiskSummary();
}
