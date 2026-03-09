package com.sesame.neobte.Controllers.Compte;

import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.Services.CompteService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/client/comptes")
public class ClientCompteController {

    private final CompteService compteService;


    @GetMapping("/utilisateur/{id}")
    public List<CompteResponseDTO> getUserAccounts(@PathVariable Long userId){
        return compteService.getComptesByUtilisateur(userId);
    }
}
