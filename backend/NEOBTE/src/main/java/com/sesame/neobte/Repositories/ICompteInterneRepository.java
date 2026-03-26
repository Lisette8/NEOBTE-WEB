package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.CompteInterne;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICompteInterneRepository extends JpaRepository<CompteInterne, Long> {
    Optional<CompteInterne> findByNom(String nom);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CompteInterne c WHERE c.nom = :nom")
    Optional<CompteInterne> findByNomForUpdate(@Param("nom") String nom);
}
