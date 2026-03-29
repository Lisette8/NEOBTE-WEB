package com.sesame.neobte.Entities.Enumeration.Loan;

public enum LoanStatut {
    PENDING_APPROVAL, // awaiting admin review
    APPROVED,         // approved, funds not yet disbursed (edge case)
    ACTIVE,           // funds disbursed, repayments running
    LATE,             // one or more installments past due
    DEFAULT,          // 3+ missed payments — user restricted from new loans
    PAID_OFF,         // fully repaid
    REJECTED          // admin rejected the request
}
