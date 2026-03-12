package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.DTO.Responses.Client.ClientResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Services.UtilisateurService;
import com.sesame.neobte.Services.UtilisateurServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/client")
public class UtilisateurController {

    UtilisateurServiceImpl utilisateurService;



    @GetMapping("/current")
    public Utilisateur getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.getUtilisateurById(userId);
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


}