package com.sesame.neobte.Controllers.DemandeCompte;

import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;
import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Services.DemandeCompteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/demandes-compte")
@AllArgsConstructor
public class ClientDemandeCompteController {

    private final DemandeCompteService demandeCompteService;
    private final JwtService jwtService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeCompteResponseDTO submitDemande(
            @Valid @RequestBody DemandeCompteCreateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));

        return demandeCompteService.submitDemande(dto, userId);
    }

    @GetMapping("/mes-demandes")
    public List<DemandeCompteResponseDTO> getMyDemandes(HttpServletRequest request) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));

        return demandeCompteService.getDemandesByUser(userId);
    }
}
