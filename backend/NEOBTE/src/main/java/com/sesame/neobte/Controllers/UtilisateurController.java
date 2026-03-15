package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.DTO.Responses.Client.ClientResponse;
import com.sesame.neobte.DTO.Responses.Virement.RibResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Services.UtilisateurService;
import com.sesame.neobte.Services.UtilisateurServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/client")
public class UtilisateurController {

    UtilisateurServiceImpl utilisateurService;
    ICompteRepository compteRepository;

    @GetMapping("/current")
    public Utilisateur getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.getUtilisateurById(userId);
    }

    @GetMapping("/rib")
    public RibResponseDTO getMyRib(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Utilisateur user = utilisateurService.getUtilisateurById(userId);
        List<Compte> comptes = compteRepository.findByUtilisateur_IdUtilisateur(userId);

        // Determine primary account (oldest COURANT active, fallback oldest active)
        Compte primary = comptes.stream()
                .filter(c -> c.getTypeCompte() == TypeCompte.COURANT && c.getStatutCompte() == StatutCompte.ACTIVE)
                .min(java.util.Comparator.comparing(Compte::getDateCreation))
                .orElse(comptes.stream()
                        .filter(c -> c.getStatutCompte() == StatutCompte.ACTIVE)
                        .min(java.util.Comparator.comparing(Compte::getDateCreation))
                        .orElse(null));

        List<RibResponseDTO.RibCompteDTO> ribComptes = comptes.stream()
                .map(c -> new RibResponseDTO.RibCompteDTO(
                        c.getIdCompte(),
                        c.getTypeCompte().name(),
                        formatRib(c.getIdCompte()),
                        c.getSolde(),
                        c.getStatutCompte().name(),
                        primary != null && primary.getIdCompte().equals(c.getIdCompte())
                )).toList();

        return new RibResponseDTO(
                user.getPrenom() + " " + user.getNom(),
                user.getEmail(),
                user.getTelephone(),
                ribComptes
        );
    }

    @PutMapping("/current")
    public ClientResponse updateProfile(@Valid @RequestBody UpdateProfileRequest dto,
                                        Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Utilisateur updated = utilisateurService.updateUtilisateur(userId, dto);
        return utilisateurService.mapToClientResponse(updated);
    }

    @PutMapping("/change-password")
    public String changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                 Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        utilisateurService.changePassword(userId, request);
        return "Password updated successfully";
    }

    @DeleteMapping("/current")
    public void deleteMyAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        utilisateurService.deleteUtilisateur(userId);
    }

    // Format: NEO-XXXX-XXXXXXXX (zero-padded account id)
    private String formatRib(Long id) {
        return String.format("NEO-%04d-%08d", id / 10000, id);
    }
}