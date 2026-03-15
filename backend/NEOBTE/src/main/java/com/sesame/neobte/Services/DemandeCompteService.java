package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.DemandeCompte.AdminDemandeDecisionDTO;
import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;

import java.util.List;

public interface DemandeCompteService {
    DemandeCompteResponseDTO submitDemande(DemandeCompteCreateDTO dto, Long userId);
    List<DemandeCompteResponseDTO> getDemandesByUser(Long userId);
    List<DemandeCompteResponseDTO> getAllDemandes();
    List<DemandeCompteResponseDTO> getDemandesByStatut(String statut);
    DemandeCompteResponseDTO approveDemande(Long demandeId, AdminDemandeDecisionDTO dto);
    DemandeCompteResponseDTO rejectDemande(Long demandeId, AdminDemandeDecisionDTO dto);
}
