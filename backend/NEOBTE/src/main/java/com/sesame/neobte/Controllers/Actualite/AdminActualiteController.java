package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.Entities.Actualite;
import com.sesame.neobte.Services.ActualiteService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/actualite")
@AllArgsConstructor
public class AdminActualiteController {

    private ActualiteService actualiteService;


    @PostMapping("/add")
    public Actualite create(
            @RequestParam String titre,
            @RequestParam String description,
            Authentication authentication
    ) {
        Long adminId = (Long) authentication.getPrincipal();
        return actualiteService.createActualite(adminId, titre, description);
    }


    @PutMapping("/update/{id}")
    public Actualite update(
            @PathVariable Long id,
            @RequestParam String titre,
            @RequestParam String description
    ) {
        return actualiteService.updateActualite(id, titre, description);
    }


    @DeleteMapping("/delete/{id}")
    public void delete(
            @PathVariable Long id
    ) {
        actualiteService.deleteActualite(id);
    }

}
