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

    //tax
    private Double feeRate;
    private Double estimatedFee;
}
