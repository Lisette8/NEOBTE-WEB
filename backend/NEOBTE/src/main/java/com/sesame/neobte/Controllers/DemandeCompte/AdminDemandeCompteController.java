package com.sesame.neobte.Controllers.DemandeCompte;

import com.sesame.neobte.DTO.Requests.DemandeCompte.AdminDemandeDecisionDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;
import com.sesame.neobte.Services.DemandeCompteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/demandes-compte")
@AllArgsConstructor
public class AdminDemandeCompteController {

    private final DemandeCompteService demandeCompteService;



    @GetMapping("/all")
    public List<DemandeCompteResponseDTO> getAllDemandes() {
        return demandeCompteService.getAllDemandes();
    }


    @GetMapping("/statut/{statut}")
    public List<DemandeCompteResponseDTO> getDemandesByStatut(@PathVariable String statut) {
        return demandeCompteService.getDemandesByStatut(statut);
    }


    @PutMapping("/{id}/approve")
    public DemandeCompteResponseDTO approveDemande(
            @PathVariable Long id,
            @Valid @RequestBody AdminDemandeDecisionDTO dto) {
        return demandeCompteService.approveDemande(id, dto);
    }


    @PutMapping("/{id}/reject")
    public DemandeCompteResponseDTO rejectDemande(
            @PathVariable Long id,
            @Valid @RequestBody AdminDemandeDecisionDTO dto) {
        return demandeCompteService.rejectDemande(id, dto);
    }
}
