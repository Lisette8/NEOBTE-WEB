package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;

import java.util.List;


public interface VirementService {
    VirementResponseDTO effectuerVirement(VirementCreateDTO dto);
    List<VirementResponseDTO> getVirementsCompte(Long compteId);

}
