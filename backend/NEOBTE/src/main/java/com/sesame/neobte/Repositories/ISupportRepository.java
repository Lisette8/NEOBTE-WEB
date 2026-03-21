package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Class.Support;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface ISupportRepository extends JpaRepository<Support, Long> {
    List<Support> findByUtilisateurIdUtilisateur(Long userId);
    void deleteByUtilisateur_IdUtilisateur(Long userId);
}
