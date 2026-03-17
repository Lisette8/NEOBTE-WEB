package com.sesame.neobte.DTO.Responses.Analytics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatDTO {
    private String date;
    private double totalAmount;
    private long count;
}
