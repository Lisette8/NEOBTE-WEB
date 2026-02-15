package com.sesame.neobte.Controllers;

import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Services.UtilisateurService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/client")
public class UtilisateurController {

    UtilisateurService utilisateurService;

    @GetMapping("/all")
    public List<Utilisateur> getAllClients() {
        return utilisateurService.getAllClients();
    }


    @GetMapping("/current")
    public Utilisateur getMyProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.getClientById(userId);
    }


    @PutMapping("/current")
    public Utilisateur updateProfile(@RequestBody Utilisateur newUtilisateur,
                                     Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();
        return utilisateurService.updateClient(userId, newUtilisateur);
    }

    @DeleteMapping("/current")
    public void deleteMyAccount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        utilisateurService.deleteClient(userId);
    }


}