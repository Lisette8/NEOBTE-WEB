package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import jakarta.transaction.Transactional;

import java.util.List;


public interface VirementService {
    RecipientPreviewDTO resolveRecipient(String identifier);
    VirementResponseDTO effectuerVirement(VirementCreateDTO dto, Long senderUserId);
    List<VirementResponseDTO> getVirementsUtilisateur(Long userId);
    List<VirementResponseDTO> getAllVirements();
    VirementResponseDTO getVirementById(Long virementId);
}
