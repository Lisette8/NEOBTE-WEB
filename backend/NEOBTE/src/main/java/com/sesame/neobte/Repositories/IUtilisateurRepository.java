package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IUtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Utilisateur findByEmail(String email);
}
