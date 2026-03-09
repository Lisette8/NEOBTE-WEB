package com.sesame.neobte.Services;


import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.Entities.Compte;

import java.util.List;

public interface CompteService {
    public CompteResponseDTO createCompte(CompteCreateDTO dto);
    public List<CompteResponseDTO> getAllComptes();
    public CompteResponseDTO getCompteById(Long id);
    public void deleteCompteById(Long id);
    List<CompteResponseDTO> getComptesByUtilisateur(Long userId);
}
