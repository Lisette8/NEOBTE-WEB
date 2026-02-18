package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.UtilisateurService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@AllArgsConstructor
@RequestMapping("/api/client")
public class UtilisateurController {

    UtilisateurService utilisateurService;



    @GetMapping("/current")
    public Utilisateur getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.getUtilisateurById(userId);
    }


    @PutMapping("/current")
    public Utilisateur updateProfile(@RequestBody UpdateProfileRequest dto,
                                     Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.updateUtilisateur(userId, dto);
    }

    @PutMapping("/change-password")
    public String changePassword(
            @RequestBody ChangePasswordRequest request,
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