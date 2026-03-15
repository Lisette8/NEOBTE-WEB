package com.sesame.neobte.Controllers.Compte;

import com.sesame.neobte.DTO.Requests.DemandeClotureCompte.DemandeClotureCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.DTO.Responses.DemandeClotureCompte.DemandeClotureResponseDTO;
import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Services.CompteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/client/comptes")
public class ClientCompteController {

    private final CompteService compteService;
    private final JwtService jwtService;

    @GetMapping("/utilisateur/{userId}")
    public List<CompteResponseDTO> getUserAccounts(@PathVariable Long userId) {
        return compteService.getComptesByUtilisateur(userId);
    }

    // Suspend own account instantly
    @PutMapping("/{compteId}/suspendre")
    public CompteResponseDTO suspendre(@PathVariable Long compteId, HttpServletRequest req) {
        Long userId = jwtService.extractUserId(req.getHeader("Authorization").substring(7));
        return compteService.suspendreCompte(compteId, userId);
    }


    // Reactivate own suspended account
    @PutMapping("/{compteId}/reactiver")
    public CompteResponseDTO reactiver(@PathVariable Long compteId, HttpServletRequest req) {
        Long userId = jwtService.extractUserId(req.getHeader("Authorization").substring(7));
        return compteService.reactiverCompte(compteId, userId);
    }

    // Request account closure (goes to admin for review)
    @PostMapping("/demande-cloture")
    public DemandeClotureResponseDTO demanderCloture(
            @Valid @RequestBody DemandeClotureCreateDTO dto,
            HttpServletRequest req) {
        Long userId = jwtService.extractUserId(req.getHeader("Authorization").substring(7));
        return compteService.demanderCloture(dto, userId);
    }

    // Cancel a pending closure request (within 48h)
    @PutMapping("/{compteId}/annuler-cloture")
    public CompteResponseDTO annulerCloture(@PathVariable Long compteId, HttpServletRequest req) {
        Long userId = jwtService.extractUserId(req.getHeader("Authorization").substring(7));
        return compteService.annulerCloture(compteId, userId);
    }

    // List own closure requests
    @GetMapping("/mes-demandes-cloture")
    public List<DemandeClotureResponseDTO> mesDemandes(HttpServletRequest req) {
        Long userId = jwtService.extractUserId(req.getHeader("Authorization").substring(7));
        return compteService.getMesDemandescloture(userId);
    }
}
