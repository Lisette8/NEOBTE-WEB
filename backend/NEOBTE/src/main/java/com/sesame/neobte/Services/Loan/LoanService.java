package com.sesame.neobte.Services.Loan;

import com.sesame.neobte.DTO.Requests.Loan.LoanProductFormDTO;
import com.sesame.neobte.DTO.Requests.Loan.LoanRequestDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanProductResponseDTO;
import com.sesame.neobte.DTO.Responses.Loan.LoanResponseDTO;

import java.util.List;

public interface LoanService {
    // Products (admin)
    List<LoanProductResponseDTO> getAllProducts();
    List<LoanProductResponseDTO> getActiveProducts();
    LoanProductResponseDTO createProduct(LoanProductFormDTO dto);
    LoanProductResponseDTO updateProduct(Long id, LoanProductFormDTO dto);
    void deleteProduct(Long id);

    // Loans (client)
    LoanResponseDTO requestLoan(LoanRequestDTO dto, Long userId);
    List<LoanResponseDTO> getMyLoans(Long userId);
    LoanResponseDTO getLoanById(Long loanId, Long userId);

    // Admin
    List<LoanResponseDTO> getAllLoans();
    LoanResponseDTO approveLoan(Long loanId, String adminNote);
    LoanResponseDTO rejectLoan(Long loanId, String motif);

    // Schedulers
    void processMonthlyRepayments();
    void applyPenalties();
}