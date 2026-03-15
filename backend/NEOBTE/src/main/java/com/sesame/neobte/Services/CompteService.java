package com.sesame.neobte.Services;


import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Requests.Compte.UpdateStatutCompteDTO;
import com.sesame.neobte.DTO.Requests.DemandeClotureCompte.DemandeClotureCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.DTO.Responses.DemandeClotureCompte.DemandeClotureResponseDTO;

import java.util.List;

public interface CompteService {

    // CRUD
    CompteResponseDTO createCompte(CompteCreateDTO dto);
    List<CompteResponseDTO> getAllComptes();
    CompteResponseDTO getCompteById(Long id);
    void deleteCompteById(Long id);
    List<CompteResponseDTO> getComptesByUtilisateur(Long userId);

    // Client — self-service status
    CompteResponseDTO suspendreCompte(Long compteId, Long userId);
    CompteResponseDTO reactiverCompte(Long compteId, Long userId);
    DemandeClotureResponseDTO demanderCloture(DemandeClotureCreateDTO dto, Long userId);
    List<DemandeClotureResponseDTO> getMesDemandescloture(Long userId);
    CompteResponseDTO annulerCloture(Long compteId, Long userId);

    // Admin — status management
    CompteResponseDTO updateStatutCompte(Long compteId, UpdateStatutCompteDTO dto);
    List<DemandeClotureResponseDTO> getAllDemandesCloture();
    DemandeClotureResponseDTO approuverCloture(Long demandeId, String commentaire);
    DemandeClotureResponseDTO rejeterCloture(Long demandeId, String commentaire);
}
