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
    private Double totalCollected;
    private Double feeRate;
    private List<FraisEntryDTO> recentTransactions;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FraisEntryDTO {
        private Long id;
        private Long virementId;
        private Double montantFrais;
        private Double tauxApplique;
        private Double montantVirement;
        private String senderName;
        private String recipientName;
        private Date date;
    }
}
