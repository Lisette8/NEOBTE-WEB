package com.sesame.neobte.Repositories;


import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
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

    //PESSIMISTIC_WRITE — prevents concurrent transfers corrupting balances
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compte c WHERE c.idCompte = :id")
    Optional<Compte> findByIdForUpdate(@Param("id") Long id);

    Optional<Compte> findById(Long id);

    List<Compte> findByUtilisateur_IdUtilisateur(Long userId);

    List<Compte> findByUtilisateur_IdUtilisateurAndTypeCompteAndStatutCompteOrderByDateCreationAsc(
            Long userId, TypeCompte typeCompte, StatutCompte statutCompte);

    List<Compte> findByUtilisateur_IdUtilisateurAndStatutCompteOrderByDateCreationAsc(
            Long userId, StatutCompte statutCompte);

    boolean existsByUtilisateur_IdUtilisateurAndTypeCompte(Long userId, TypeCompte typeCompte);

}
