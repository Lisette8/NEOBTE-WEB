package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICompteRepository extends JpaRepository<Client, Long> {
}
