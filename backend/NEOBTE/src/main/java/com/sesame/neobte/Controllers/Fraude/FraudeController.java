package com.sesame.neobte.Controllers.Fraude;

import com.sesame.neobte.DTO.Requests.Fraude.FraudeConfigUpdateDTO;
import com.sesame.neobte.DTO.Requests.Fraude.FraudeReviewDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeAlerteResponseDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeConfigResponseDTO;
import com.sesame.neobte.Security.Services.Fraude.FraudeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/fraude")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FraudeController {

    private final FraudeService fraudeService;

    @GetMapping("/alertes")
    public List<FraudeAlerteResponseDTO> getAll() {
        return fraudeService.getAllAlertes();
    }

    @GetMapping("/alertes/open")
    public List<FraudeAlerteResponseDTO> getOpen() {
        return fraudeService.getOpenAlertes();
    }

    @GetMapping("/alertes/count-open")
    public Map<String, Long> countOpen() {
        return Map.of("count", fraudeService.countOpen());
    }

    @PutMapping("/alertes/{id}/review")
    public FraudeAlerteResponseDTO review(
            @PathVariable Long id,
            @Valid @RequestBody FraudeReviewDTO dto) {
        return fraudeService.reviewAlerte(id, dto);
    }

    @GetMapping("/config")
    public FraudeConfigResponseDTO getConfig() {
        return fraudeService.getConfig();
    }

    @PutMapping("/config")
    public FraudeConfigResponseDTO updateConfig(@Valid @RequestBody FraudeConfigUpdateDTO dto) {
        return fraudeService.updateConfig(dto);
    }
}