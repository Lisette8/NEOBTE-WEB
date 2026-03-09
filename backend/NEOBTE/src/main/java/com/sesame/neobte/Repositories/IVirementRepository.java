package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVirementRepository extends JpaRepository<Virement, Long> {

    List<Virement> findByCompteDeIdCompte(Long compteId);
    List<Virement> findByCompteAIdCompte(Long compteId);
    Optional<Virement> findByIdempotencyKey(String idempotencyKey);
}
