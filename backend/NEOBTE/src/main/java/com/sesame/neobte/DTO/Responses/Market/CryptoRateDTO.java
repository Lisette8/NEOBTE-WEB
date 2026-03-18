package com.sesame.neobte.DTO.Responses.Market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CryptoRateDTO {
    private String symbol;
    private Double priceUsd;
    private Double priceEur;
    private Double priceTnd;
}

