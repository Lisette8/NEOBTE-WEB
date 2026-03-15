package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.CompteInterne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ICompteInterneRepository extends JpaRepository<CompteInterne, Long> {
    Optional<CompteInterne> findByNom(String nom);
}
