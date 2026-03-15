package com.sesame.neobte.Controllers.Compte;


import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Requests.Compte.UpdateStatutCompteDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.DTO.Responses.DemandeClotureCompte.DemandeClotureResponseDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Services.CompteService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/comptes")
@AllArgsConstructor
public class AdminCompteController {

    private final CompteService compteService;

    @PostMapping
    public CompteResponseDTO createCompte(@RequestBody CompteCreateDTO dto) {
        return compteService.createCompte(dto);
    }

    @GetMapping("/all")
    public List<CompteResponseDTO> getAllComptes() {
        return compteService.getAllComptes();
    }

    @GetMapping("/{id}")
    public CompteResponseDTO getCompteById(@PathVariable Long id) {
        return compteService.getCompteById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteCompte(@PathVariable Long id) {
        compteService.deleteCompteById(id);
    }

    // Admin changes account status (ACTIVE / SUSPENDED / BLOCKED / CLOSED)
    @PutMapping("/{id}/statut")
    public CompteResponseDTO updateStatut(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatutCompteDTO dto) {
        return compteService.updateStatutCompte(id, dto);
    }

    // Closure requests management
    @GetMapping("/demandes-cloture")
    public List<DemandeClotureResponseDTO> getAllDemandesCloture() {
        return compteService.getAllDemandesCloture();
    }

    @PutMapping("/demandes-cloture/{id}/approuver")
    public DemandeClotureResponseDTO approuver(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String commentaire = body != null ? body.get("commentaire") : null;
        return compteService.approuverCloture(id, commentaire);
    }

    @PutMapping("/demandes-cloture/{id}/rejeter")
    public DemandeClotureResponseDTO rejeter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return compteService.rejeterCloture(id, body.get("commentaire"));
    }
}
