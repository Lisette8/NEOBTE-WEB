package com.sesame.neobte.Controllers.Virement;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Services.VirementService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/client/virement")
@AllArgsConstructor
public class ClientVirementController {

    private VirementService virementService;

    @PostMapping("/transfer")
    public VirementResponseDTO transfer(
            @RequestBody VirementCreateDTO dto
    ) {
        return virementService.effectuerVirement(dto);
    }


    @GetMapping("/history/{compteId}")
    public List<VirementResponseDTO> history(
            @PathVariable Long compteId
    ) {
        return virementService.getVirementsCompte(compteId);
    }



}
