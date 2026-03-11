package com.sesame.neobte.Controllers.Virement;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Services.VirementService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/client/virements")
@AllArgsConstructor
public class ClientVirementController {

    private VirementService virementService;

    @PostMapping("/transfer")
    public VirementResponseDTO transfer(
            @Valid @RequestBody VirementCreateDTO dto //I added valid to make sure it's validated, it's basically another layer of protection eq validation
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
