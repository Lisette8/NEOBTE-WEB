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
    private Long   primaryCompteId;
    private String primaryCompteType;
    private boolean found;
    private String photoUrl;

    // Fee info (based on SENDER's account type)
    private Double feeRate;
    private Double estimatedFee;

    // Sender account-type limits (surfaced so UI can warn before submit)
    private Double  largeTransferThreshold;
    private Double  dailyAmountLimit;
    private Integer dailyCountLimit;
    private Integer monthlyCountLimit;
    private Boolean canSendExternal;
}
