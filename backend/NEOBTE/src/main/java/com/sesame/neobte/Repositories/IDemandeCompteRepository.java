package com.sesame.neobte.Repositories;


import com.sesame.neobte.Entities.Class.DemandeCompte;
import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDemandeCompteRepository extends JpaRepository<DemandeCompte, Long> {
    List<DemandeCompte> findByUtilisateur_IdUtilisateurOrderByDateDemandeDesc(Long userId);
    List<DemandeCompte> findByStatutDemandeOrderByDateDemandeAsc(StatutDemande statut);
    List<DemandeCompte> findAllByOrderByStatutDemandeAscDateDemandeDesc();

    void deleteByUtilisateur_IdUtilisateur(Long userId);

    boolean existsByUtilisateur_IdUtilisateurAndTypeCompteAndStatutDemande(
            Long userId,
            TypeCompte typeCompte,
            StatutDemande statut
    );


}

