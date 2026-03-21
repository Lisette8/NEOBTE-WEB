package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.ReferralReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IReferralRewardRepository extends JpaRepository<ReferralReward, Long> {
    List<ReferralReward> findByReferrer_IdUtilisateurOrderByDateReferralDesc(Long referrerId);
    boolean existsByReferred_IdUtilisateur(Long referredId);
    long countByReferrer_IdUtilisateur(Long referrerId);
    Optional<ReferralReward> findByReferred_IdUtilisateur(Long referredId);
    void deleteByReferrer_IdUtilisateur(Long userId);
    void deleteByReferred_IdUtilisateur(Long userId);

    // Nullify FK instead of deleting — preserves audit trail and referrer's premium
    @Modifying
    @Query("UPDATE ReferralReward r SET r.referrer = null WHERE r.referrer.idUtilisateur = :userId")
    void nullifyReferrer(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE ReferralReward r SET r.referred = null WHERE r.referred.idUtilisateur = :userId")
    void nullifyReferred(@Param("userId") Long userId);
}
