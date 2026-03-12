package com.sesame.neobte.Controllers.DemandeCompte;

import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;
import com.sesame.neobte.Services.DemandeCompteService;
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


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemandeCompteResponseDTO submitDemande(@Valid @RequestBody DemandeCompteCreateDTO dto) {
        return demandeCompteService.submitDemande(dto);
    }

    @GetMapping("/utilisateur/{userId}")
    public List<DemandeCompteResponseDTO> getMyDemandes(@PathVariable Long userId) {
        return demandeCompteService.getDemandesByUser(userId);
    }
}
