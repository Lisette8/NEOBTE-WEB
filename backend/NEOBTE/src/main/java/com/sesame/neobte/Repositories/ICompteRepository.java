package com.sesame.neobte.Repositories;


import com.sesame.neobte.Entities.Compte;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICompteRepository extends JpaRepository<Compte, Long> {

    //PESSIMISTIC_WRITE
    //this is so important to prevent two accounts from sending money to the same account at the same time which creates confusion eq errors and bad values...
    //this is hugely important in modern fintech apps
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compte c WHERE c.idCompte = :id")
    Optional<Compte> findByIdForUpdate(@Param("id") Long id);

    Optional<Compte> findById(Long id);

}
