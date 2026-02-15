package com.sesame.neobte.Repositories;

import com.sesame.neobte.Entities.Virement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IVirementRepository extends JpaRepository<Virement, Long> {
}
