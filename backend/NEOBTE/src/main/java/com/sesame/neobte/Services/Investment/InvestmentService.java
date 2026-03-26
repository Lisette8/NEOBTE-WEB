package com.sesame.neobte.Services.Investment;

import com.sesame.neobte.DTO.Requests.Investment.InvestmentCreateDTO;
import com.sesame.neobte.DTO.Requests.Investment.InvestmentPlanCreateDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentPlanResponseDTO;
import com.sesame.neobte.DTO.Responses.Investment.InvestmentResponseDTO;

import java.util.List;

public interface InvestmentService {
    // Plans (admin)
    List<InvestmentPlanResponseDTO> getAllPlans();
    List<InvestmentPlanResponseDTO> getActivePlans();
    InvestmentPlanResponseDTO createPlan(InvestmentPlanCreateDTO dto);
    InvestmentPlanResponseDTO updatePlan(Long id, InvestmentPlanCreateDTO dto);
    void deletePlan(Long id);

    // Investments (client)
    InvestmentResponseDTO subscribe(InvestmentCreateDTO dto, Long userId);
    InvestmentResponseDTO cancel(Long investmentId, Long userId);
    List<InvestmentResponseDTO> getMyInvestments(Long userId);

    // Admin
    List<InvestmentResponseDTO> getAllInvestments();

    // Scheduler
    void matureAll();
}