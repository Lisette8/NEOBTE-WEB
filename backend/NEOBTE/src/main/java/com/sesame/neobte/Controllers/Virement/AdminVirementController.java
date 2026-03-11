package com.sesame.neobte.Controllers.Virement;


import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Services.VirementService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/virements")
@AllArgsConstructor
public class AdminVirementController {

    private VirementService virementService;

    @GetMapping("/all")
    public List<VirementResponseDTO> getAllVirements() {

        return virementService.getAllVirements();
    }

    @GetMapping("/{id}")
    public VirementResponseDTO getVirementById(@PathVariable Long id) {

        return virementService.getVirementById(id);
    }
}
