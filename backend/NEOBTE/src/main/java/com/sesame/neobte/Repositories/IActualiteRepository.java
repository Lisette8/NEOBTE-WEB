package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Actualite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IActualiteRepository extends JpaRepository<Actualite, Long> {
    List<Actualite> findTop10ByOrderByDateCreationActualiteDesc();
}
