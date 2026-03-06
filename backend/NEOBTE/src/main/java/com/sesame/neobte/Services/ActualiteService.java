package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Entities.Actualite;

import java.util.*;

public interface ActualiteService {

    ActualiteResponseDTO createActualite(Long adminId, String titre, String description);
    List<ActualiteResponseDTO> getAll();
    ActualiteResponseDTO getById(Long id);
    ActualiteResponseDTO updateActualite(Long id, String titre, String description);
    void deleteActualite(Long id);
}
