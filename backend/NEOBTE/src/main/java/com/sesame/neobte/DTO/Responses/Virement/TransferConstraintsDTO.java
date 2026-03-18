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
    private Double largeTransferThreshold;
    private Double dailyAmountLimit;
    private Integer dailyCountLimit;
}

