package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByEmail(String email);
    Utilisateur findByTelephone(String telephone);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);
    boolean existsByUsername(String username);
    boolean existsByCin(String cin);

}
