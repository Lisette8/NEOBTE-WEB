package com.sesame.neobte.DTO.Responses.Market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketRatesResponseDTO {
    /** ISO timestamp */
    private String generatedAt;
    /** True when returned from cache after a provider failure */
    private boolean stale;
    /** USD->TND reference used for crypto TND conversion */
    private Double usdToTnd;
    private List<FxRateDTO> fx;
    private List<CryptoRateDTO> crypto;
}

