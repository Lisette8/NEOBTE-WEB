package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.ActualiteReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IActualiteReactionRepository extends JpaRepository<ActualiteReaction, Long> {

    Optional<ActualiteReaction> findByActualite_IdActualiteAndUtilisateur_IdUtilisateur(Long actualiteId, Long userId);

    void deleteByActualite_IdActualite(Long actualiteId);

    @Query("SELECT r.actualite.idActualite, r.emoji, COUNT(r) " +
            "FROM ActualiteReaction r " +
            "WHERE r.actualite.idActualite IN :ids " +
            "GROUP BY r.actualite.idActualite, r.emoji")
    List<Object[]> countByActualiteIdsGrouped(@Param("ids") List<Long> ids);

    @Query("SELECT r FROM ActualiteReaction r " +
            "WHERE r.utilisateur.idUtilisateur = :userId " +
            "AND r.actualite.idActualite IN :ids")
    List<ActualiteReaction> findUserReactionsForPosts(@Param("userId") Long userId, @Param("ids") List<Long> ids);
}
