package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface IUtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByEmail(String email);
    Utilisateur findByTelephone(String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByUsername(String username);
    boolean existsByCin(String cin);

    long countByRole(Role role);

    /** Monthly user registration counts (YYYY-MM, count) — Oracle TO_CHAR */
    @Query(value = "SELECT TO_CHAR(date_creation_compte, 'YYYY-MM') AS month, COUNT(*) AS cnt " +
            "FROM utilisateur " +
            "WHERE date_creation_compte >= :since " +
            "GROUP BY TO_CHAR(date_creation_compte, 'YYYY-MM') " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> monthlyUserGrowth(@Param("since") Date since);
}
