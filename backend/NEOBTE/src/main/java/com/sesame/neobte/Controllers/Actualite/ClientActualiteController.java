package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Entities.Actualite;
import com.sesame.neobte.Services.ActualiteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/actualite")
@AllArgsConstructor

public class ClientActualiteController {

    private ActualiteService actualiteService;


    @GetMapping("/all")
    public Page<ActualiteResponseDTO> getAllActualites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return actualiteService.getAll(page, size);
    }

    @GetMapping("/{id}")
    public ActualiteResponseDTO getActualiteById(@PathVariable Long id) {
        return actualiteService.getById(id);
    }
}
