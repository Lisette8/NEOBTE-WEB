package com.sesame.neobte.Controllers.Investment;

import com.sesame.neobte.DTO.Requests.Investment.InvestmentPlanCreateDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentPlanResponseDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentResponseDTO;
import com.sesame.neobte.Services.Investment.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/investments")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminInvestmentController {

    private final InvestmentService investmentService;

    @GetMapping("/plans")
    public List<InvestmentPlanResponseDTO> getAllPlans() {
        return investmentService.getAllPlans();
    }

    @PostMapping("/plans")
    public InvestmentPlanResponseDTO createPlan(@Valid @RequestBody InvestmentPlanCreateDTO dto) {
        return investmentService.createPlan(dto);
    }

    @PutMapping("/plans/{id}")
    public InvestmentPlanResponseDTO updatePlan(
            @PathVariable Long id, @Valid @RequestBody InvestmentPlanCreateDTO dto) {
        return investmentService.updatePlan(id, dto);
    }

    @DeleteMapping("/plans/{id}")
    public Map<String, String> deletePlan(@PathVariable Long id) {
        investmentService.deletePlan(id);
        return Map.of("message", "Plan supprimé ou désactivé.");
    }

    @GetMapping("/all")
    public List<InvestmentResponseDTO> getAllInvestments() {
        return investmentService.getAllInvestments();
    }
}