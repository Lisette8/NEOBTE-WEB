package com.sesame.neobte.Entities.Enumeration.Loan;

public enum RepaymentStatut {
    PENDING,  // due in the future
    PAID,     // collected successfully
    LATE,     // past due date, within grace period — no penalty yet
    FAILED,   // grace period expired, penalty applied
    WAIVED    // admin-waived (exceptional circumstances)
}
