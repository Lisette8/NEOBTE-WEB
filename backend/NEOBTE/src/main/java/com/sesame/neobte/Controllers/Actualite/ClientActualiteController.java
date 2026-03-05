package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Entities.Actualite;
import com.sesame.neobte.Services.ActualiteService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client/actualite")
@AllArgsConstructor

public class ClientActualiteController {

    private ActualiteService actualiteService;


    @GetMapping("/all")
    public List<ActualiteResponseDTO> getAllActualites() {
        return actualiteService.getAll();
    }
}
