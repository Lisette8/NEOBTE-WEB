package com.sesame.neobte.Entities.Enumeration.Fraude;

public enum FraudeAlertType {
    SUSPICIOUS_HOUR,       // transfer between 02:00–04:00
    DAILY_COUNT_EXCEEDED,  // too many transfers in 24h
    DAILY_AMOUNT_EXCEEDED, // total daily amount too high
    RAPID_SUCCESSION,      // multiple transfers within short window
    LARGE_SINGLE_TRANSFER  // single transfer above threshold
}
