package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.DTO.Requests.Actualite.ActualiteCreateDTO;
import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Services.ActualiteService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/admin/actualite")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
public class AdminActualiteController {

    private ActualiteService actualiteService;


    @PostMapping("/add")
    public ActualiteResponseDTO create(
            @RequestBody ActualiteCreateDTO dto,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();

        return actualiteService.createActualite(adminId, dto.getTitre(), dto.getDescription());
    }


    @PutMapping("/update/{id}")
    public ActualiteResponseDTO update(
            @PathVariable Long id,
            @RequestBody ActualiteCreateDTO dto
    ) {
        return actualiteService.updateActualite(id, dto.getTitre(), dto.getDescription());
    }


    @DeleteMapping("/delete/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        actualiteService.deleteActualite(id);
    }

}
