package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAdministrateurRepository extends JpaRepository<Administrateur, Long> {
}
