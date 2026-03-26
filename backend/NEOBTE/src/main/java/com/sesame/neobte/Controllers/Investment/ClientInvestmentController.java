package com.sesame.neobte.Controllers.Investment;

import com.sesame.neobte.DTO.Requests.Investment.InvestmentCreateDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentPlanResponseDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentResponseDTO;
import com.sesame.neobte.Services.Investment.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/investments")
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
@RequiredArgsConstructor
public class ClientInvestmentController {

    private final InvestmentService investmentService;

    @GetMapping("/plans")
    public List<InvestmentPlanResponseDTO> getActivePlans() {
        return investmentService.getActivePlans();
    }

    @PostMapping("/subscribe")
    public InvestmentResponseDTO subscribe(
            @Valid @RequestBody InvestmentCreateDTO dto, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return investmentService.subscribe(dto, userId);
    }

    @PostMapping("/{id}/cancel")
    public InvestmentResponseDTO cancel(@PathVariable Long id, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return investmentService.cancel(id, userId);
    }

    @GetMapping("/my")
    public List<InvestmentResponseDTO> getMyInvestments(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return investmentService.getMyInvestments(userId);
    }
}