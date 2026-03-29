package com.sesame.neobte.Controllers.Virement;

import com.sesame.neobte.DTO.Responses.Virement.TreasuryResponseDTO;
import com.sesame.neobte.Entities.Class.FraisTransaction;
import com.sesame.neobte.Entities.Enumeration.Investment.InvestmentStatut;
import com.sesame.neobte.Repositories.ICompteInterneRepository;
import com.sesame.neobte.Repositories.IFraisTransactionRepository;
import com.sesame.neobte.Repositories.Investment.IInvestmentRepository;
import com.sesame.neobte.Repositories.Loan.ILoanRepository;
import com.sesame.neobte.Services.Investment.InvestmentServiceImpl;
import com.sesame.neobte.Services.Loan.LoanServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/treasury")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class TreasuryController {

    private final ICompteInterneRepository compteInterneRepository;
    private final IFraisTransactionRepository fraisTransactionRepository;
    private final IInvestmentRepository investmentRepository;
    private final ILoanRepository loanRepository;

    @Value("${neobte.transfer.fee-rate:0.005}")
    private double feeRate;

    @Value("${neobte.investment.reserve-rate:0.15}")
    private double reserveRate;

    @GetMapping
    public TreasuryResponseDTO getTreasury() {

        // ── Revenue account ───────────────────────────────────────────────
        double fees = compteInterneRepository.findByNom(InvestmentServiceImpl.ACCOUNT_FEES)
                .map(c -> c.getSolde()).orElse(0.0);

        // ── Investment pool accounts ──────────────────────────────────────
        double investmentPool = compteInterneRepository.findByNom(InvestmentServiceImpl.ACCOUNT_INVESTMENTS)
                .map(c -> c.getSolde()).orElse(0.0);
        double reserves = compteInterneRepository.findByNom(InvestmentServiceImpl.ACCOUNT_RESERVES)
                .map(c -> c.getSolde()).orElse(0.0);
        double deployed = compteInterneRepository.findByNom(InvestmentServiceImpl.ACCOUNT_DEPLOYED)
                .map(c -> c.getSolde()).orElse(0.0);

        // ── Investment stats ──────────────────────────────────────────────
        long activeInvestments = investmentRepository.countByStatut(InvestmentStatut.ACTIVE);
        double totalInterestPaid = investmentRepository.totalInterestPaid();

        // ── Fee audit trail ───────────────────────────────────────────────
        List<FraisTransaction> fraisList = fraisTransactionRepository.findAllByOrderByDateCreationDesc();
        List<TreasuryResponseDTO.FraisEntryDTO> entries = fraisList.stream()
                .limit(100)
                .map(f -> {
                    var v = f.getVirement();
                    String sender    = v.getCompteDe().getUtilisateur() != null
                            ? v.getCompteDe().getUtilisateur().getPrenom() + " " + v.getCompteDe().getUtilisateur().getNom()
                            : "—";
                    String recipient = v.getCompteA().getUtilisateur() != null
                            ? v.getCompteA().getUtilisateur().getPrenom() + " " + v.getCompteA().getUtilisateur().getNom()
                            : "—";
                    return new TreasuryResponseDTO.FraisEntryDTO(
                            f.getId(), v.getIdVirement(), f.getMontantFrais(),
                            f.getTauxApplique(), v.getMontant(), sender, recipient, f.getDateCreation());
                }).toList();

        // ── Loan stats ────────────────────────────────────────────────────
        long activeLoans         = loanRepository.countActiveLoans();
        double totalOutstanding  = loanRepository.totalOutstanding();
        double totalRepaid       = loanRepository.totalRepaid();
        double totalPenalties    = loanRepository.totalPenalties();
        double loanPoolBalance   = compteInterneRepository.findByNom(LoanServiceImpl.ACCOUNT_LOANS)
                .map(c -> c.getSolde()).orElse(0.0);

        return new TreasuryResponseDTO(
                fees, feeRate,
                investmentPool, reserves, deployed, reserveRate,
                activeInvestments, totalInterestPaid,
                activeLoans, totalOutstanding, totalRepaid, totalPenalties, loanPoolBalance,
                entries);
    }
}