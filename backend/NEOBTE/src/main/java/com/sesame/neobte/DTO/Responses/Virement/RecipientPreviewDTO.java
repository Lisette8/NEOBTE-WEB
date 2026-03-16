package com.sesame.neobte.DTO.Responses.Virement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecipientPreviewDTO {
    private String displayName;
    private String maskedIdentifier;
    private Long primaryCompteId;
    private String primaryCompteType;
    private boolean found;

    // Fee info
    private Double feeRate;
    private Double estimatedFee;

    // Fraud limits — surfaced to the client so the UI can warn before submitting
    private Double largeTransferThreshold;
    private Double dailyAmountLimit;
    private Integer dailyCountLimit;
}
