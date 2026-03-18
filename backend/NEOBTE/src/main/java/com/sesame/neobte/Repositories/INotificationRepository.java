package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface INotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUtilisateur_IdUtilisateurOrderByDateCreationDesc(Long userId, Pageable pageable);

    Page<Notification> findByUtilisateur_IdUtilisateurAndLuFalseOrderByDateCreationDesc(Long userId, Pageable pageable);

    long countByUtilisateur_IdUtilisateurAndLuFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.lu = true WHERE n.utilisateur.idUtilisateur = :userId AND n.lu = false")
    int markAllRead(@Param("userId") Long userId);
}

