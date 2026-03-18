package com.sesame.neobte.DTO.Responses.Market;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FxRateDTO {
    /** Example: "EUR/TND" meaning 1 EUR = X TND */
    private String pair;
    private Double rate;
}

