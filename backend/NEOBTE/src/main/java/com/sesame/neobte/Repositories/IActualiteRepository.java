package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Actualite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface IActualiteRepository extends JpaRepository<Actualite, Long> {
}
