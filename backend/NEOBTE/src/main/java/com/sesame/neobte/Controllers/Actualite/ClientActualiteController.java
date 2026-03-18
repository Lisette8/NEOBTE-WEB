package com.sesame.neobte.Controllers.Actualite;

import com.sesame.neobte.DTO.Responses.Actualite.ActualiteResponseDTO;
import com.sesame.neobte.Services.ActualiteService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/actualite")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")

public class ClientActualiteController {

    private ActualiteService actualiteService;


    @GetMapping("/all")
    public Page<ActualiteResponseDTO> getAllActualites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return actualiteService.getAll(page, size, userId);
    }

    @GetMapping("/{id}")
    public ActualiteResponseDTO getActualiteById(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return actualiteService.getById(id, userId);
    }

    @PostMapping("/{id}/reaction")
    public ActualiteResponseDTO react(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        // Backward compatible: older clients send "emoji", newer send "reaction"
        String reaction = body.getOrDefault("reaction", body.get("emoji"));
        return actualiteService.react(id, userId, reaction);
    }
}
