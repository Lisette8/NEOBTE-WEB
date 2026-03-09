package com.sesame.neobte.Controllers.Compte;


import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Services.CompteService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void deleteCompteById(@PathVariable Long id) {
        compteService.deleteCompteById(id);
    }
}
