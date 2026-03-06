package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Actualite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface IActualiteRepository extends JpaRepository<Actualite, Long> {
    Page<Actualite> findAllByOrderByDateCreationActualiteDesc(Pageable pageable);
}
