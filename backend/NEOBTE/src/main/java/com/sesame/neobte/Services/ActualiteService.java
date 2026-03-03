package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Actualite;

import java.util.*;

public interface ActualiteService {

    Actualite createActualite(Long adminId, String titre, String description);
    List<Actualite> getAll();
    Actualite updateActualite(Long id, String titre, String description);
    void deleteActualite(Long id);
}
