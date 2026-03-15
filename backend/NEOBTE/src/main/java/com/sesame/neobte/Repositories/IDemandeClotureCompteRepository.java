package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.DemandeClotureCompte;
import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDemandeClotureCompteRepository extends JpaRepository<DemandeClotureCompte, Long> {
    List<DemandeClotureCompte> findAllByOrderByStatutAscDateDemandeDesc();
    List<DemandeClotureCompte> findByUtilisateur_IdUtilisateur(Long userId);
    boolean existsByCompte_IdCompteAndStatut(Long compteId, StatutDemande statut);
}
