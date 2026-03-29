package com.sesame.neobte.Controllers.Loan;

import com.sesame.neobte.DTO.Requests.Loan.LoanProductFormDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanProductResponseDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanResponseDTO;
import com.sesame.neobte.Services.Loan.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/loans")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminLoanController {

    private final LoanService loanService;

    @GetMapping("/products")
    public List<LoanProductResponseDTO> getAllProducts() { return loanService.getAllProducts(); }

    @PostMapping("/products")
    public LoanProductResponseDTO createProduct(@Valid @RequestBody LoanProductFormDTO dto) {
        return loanService.createProduct(dto);
    }
    @PutMapping("/products/{id}")
    public LoanProductResponseDTO updateProduct(@PathVariable Long id, @Valid @RequestBody LoanProductFormDTO dto) {
        return loanService.updateProduct(id, dto);
    }
    @DeleteMapping("/products/{id}")
    public Map<String, String> deleteProduct(@PathVariable Long id) {
        loanService.deleteProduct(id);
        return Map.of("message", "Produit supprimé ou désactivé.");
    }

    @GetMapping("/all")
    public List<LoanResponseDTO> getAllLoans() { return loanService.getAllLoans(); }

    @PutMapping("/{id}/approve")
    public LoanResponseDTO approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String note = body != null ? body.getOrDefault("adminNote", "") : "";
        return loanService.approveLoan(id, note);
    }

    @PutMapping("/{id}/reject")
    public LoanResponseDTO reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return loanService.rejectLoan(id, body.getOrDefault("motif", "Dossier incomplet."));
    }
}
