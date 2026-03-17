package com.sesame.neobte.DTO.Responses.Analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatDTO {
    private String month;
    private long count;
}
