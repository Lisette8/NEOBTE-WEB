package com.sesame.neobte.Controllers.Loan;

import com.sesame.neobte.DTO.Requests.Loan.LoanRequestDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanProductResponseDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanResponseDTO;
import com.sesame.neobte.Services.Loan.LoanService;
import com.sesame.neobte.Services.Loan.LoanServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/loans")
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
@RequiredArgsConstructor
public class ClientLoanController {

    private final LoanService loanService;

    @GetMapping("/products")
    public List<LoanProductResponseDTO> getProducts() {
        return loanService.getActiveProducts();
    }

    /** Loan simulator — returns estimated mensualite + totalDu without persisting */
    @GetMapping("/simulate")
    public Map<String, Object> simulate(
            @RequestParam double montant,
            @RequestParam double tauxAnnuel,
            @RequestParam int dureeEnMois) {
        double mensualite  = LoanServiceImpl.calcMensualite(montant, tauxAnnuel, dureeEnMois);
        double totalDu     = Math.round(mensualite * dureeEnMois * 1000.0) / 1000.0;
        double totalInteret = Math.round((totalDu - montant) * 1000.0) / 1000.0;
        return Map.of(
                "montant", montant, "tauxAnnuel", tauxAnnuel, "dureeEnMois", dureeEnMois,
                "mensualite", mensualite, "totalDu", totalDu, "totalInteret", totalInteret);
    }

    @PostMapping("/request")
    public LoanResponseDTO requestLoan(@Valid @RequestBody LoanRequestDTO dto, Authentication auth) {
        return loanService.requestLoan(dto, (Long) auth.getPrincipal());
    }

    @GetMapping("/my")
    public List<LoanResponseDTO> getMyLoans(Authentication auth) {
        return loanService.getMyLoans((Long) auth.getPrincipal());
    }

    @GetMapping("/{id}")
    public LoanResponseDTO getLoan(@PathVariable Long id, Authentication auth) {
        return loanService.getLoanById(id, (Long) auth.getPrincipal());
    }
}
