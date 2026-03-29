package com.sesame.neobte.DTO.Responses.Virement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TreasuryResponseDTO {

    // Revenue
    private Double totalCollected;   // NEOBTE_FEES
    private Double feeRate;

    // Investment pool (balance sheet)
    private Double investmentPool;   // NEOBTE_INVESTMENTS — total client principal held
    private Double reserves;         // NEOBTE_RESERVES    — liquid buffer (15%)
    private Double deployed;         // NEOBTE_DEPLOYED    — capital available for lending
    private Double reserveRate;      // configured rate (e.g. 0.15)

    // Investment stats
    private Long   activeInvestments;
    private Double totalInterestPaid;

    // Loan stats
    private Long   activeLoans;
    private Double totalLentOutstanding; // current outstanding loan principal
    private Double totalLoanRepaid;      // cumulative repaid
    private Double totalLoanPenalties;   // cumulative penalties collected
    private Double loanPoolBalance;      // NEOBTE_LOANS tracking account  // cumulative interest paid out at maturity

    private List<FraisEntryDTO> recentTransactions;

    @Getter @Setter @AllArgsConstructor @NoArgsConstructor
    public static class FraisEntryDTO {
        private Long   id;
        private Long   virementId;
        private Double montantFrais;
        private Double tauxApplique;
        private Double montantVirement;
        private String senderName;
        private String recipientName;
        private Date   date;
    }
}