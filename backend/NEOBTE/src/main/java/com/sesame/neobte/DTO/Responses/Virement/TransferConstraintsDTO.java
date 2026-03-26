package com.sesame.neobte.DTO.Responses.Virement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferConstraintsDTO {
    private Double feeRate;
    private Double largeTransferThreshold;   // single-transfer max
    private Double dailyAmountLimit;
    private Integer dailyCountLimit;
    private Integer monthlyCountLimit;
    private Boolean canSendExternal;         // false for EPARGNE
    private String  accountTypePurpose;      // human-readable purpose text
    private String  accountTypeLabel;        // e.g. "compte épargne"
}

