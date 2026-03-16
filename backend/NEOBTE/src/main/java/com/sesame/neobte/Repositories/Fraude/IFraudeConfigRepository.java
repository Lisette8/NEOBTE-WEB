package com.sesame.neobte.Repositories.Fraude;

import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFraudeConfigRepository extends JpaRepository<FraudeConfig, Long> {}
