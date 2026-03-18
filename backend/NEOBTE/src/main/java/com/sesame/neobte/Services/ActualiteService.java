package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.DTO.Requests.Actualite.ActualiteCreateDTO;
import org.springframework.data.domain.Page;

public interface ActualiteService {

    ActualiteResponseDTO createActualite(Long adminId, ActualiteCreateDTO dto, String imageUrl);
    Page<ActualiteResponseDTO> getAll(int page, int size, Long callerUserId);
    ActualiteResponseDTO getById(Long id, Long callerUserId);
    ActualiteResponseDTO updateActualite(Long id, ActualiteCreateDTO dto, String imageUrl);
    void deleteActualite(Long id);

    ActualiteResponseDTO react(Long actualiteId, Long userId, String reaction);
}
