package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import org.springframework.data.domain.Page;

public interface ActualiteService {

    ActualiteResponseDTO createActualite(Long adminId, String titre, String description);
    Page<ActualiteResponseDTO> getAll(int page, int size);
    ActualiteResponseDTO getById(Long id);
    ActualiteResponseDTO updateActualite(Long id, String titre, String description);
    void deleteActualite(Long id);
}
