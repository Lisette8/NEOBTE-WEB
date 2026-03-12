package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.DemandeCompte.AdminDemandeDecisionDTO;
import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;

import java.util.List;

public interface DemandeCompteService {
    // Client: submit a new account request
    DemandeCompteResponseDTO submitDemande(DemandeCompteCreateDTO dto);

    // Client: view own requests
    List<DemandeCompteResponseDTO> getDemandesByUser(Long userId);

    // Admin: view all requests (optionally filtered by status)
    List<DemandeCompteResponseDTO> getAllDemandes();
    List<DemandeCompteResponseDTO> getDemandesByStatut(String statut);

    // Admin: approve → creates the Compte, notifies client
    DemandeCompteResponseDTO approveDemande(Long demandeId, AdminDemandeDecisionDTO dto);

    // Admin: reject → saves reason, notifies client
    DemandeCompteResponseDTO rejectDemande(Long demandeId, AdminDemandeDecisionDTO dto);
}
