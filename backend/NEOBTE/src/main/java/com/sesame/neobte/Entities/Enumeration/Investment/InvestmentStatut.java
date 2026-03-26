package com.sesame.neobte.Entities.Enumeration.Investment;

public enum InvestmentStatut {
    ACTIVE,      // funds locked, earning interest
    COMPLETED,   // matured — principal + profit returned to account
    CANCELLED    // cancelled before maturity — principal returned, no interest
}