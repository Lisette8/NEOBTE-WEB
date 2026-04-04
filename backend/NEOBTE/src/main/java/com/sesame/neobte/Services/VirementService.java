package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.InternalTransferCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementHistoryFilterDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.TransferConstraintsDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementHistoryPageDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;

import java.util.List;


public interface VirementService {
    RecipientPreviewDTO resolveRecipient(String identifier, Long senderUserId);
    TransferConstraintsDTO getConstraints(Long senderUserId, boolean internal);
    VirementResponseDTO effectuerVirement(VirementCreateDTO dto, Long senderUserId);
    VirementResponseDTO effectuerVirementInterne(InternalTransferCreateDTO dto, Long senderUserId);
    List<VirementResponseDTO> getVirementsUtilisateur(Long userId);
    VirementHistoryPageDTO getFilteredHistory(Long userId, VirementHistoryFilterDTO filter);
    byte[] exportHistoryCsv(Long userId, VirementHistoryFilterDTO filter);
    List<VirementResponseDTO> getAllVirements();
    VirementResponseDTO getVirementById(Long virementId);
}