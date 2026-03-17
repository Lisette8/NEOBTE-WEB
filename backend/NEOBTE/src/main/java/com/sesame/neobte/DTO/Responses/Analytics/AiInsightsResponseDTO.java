package com.sesame.neobte.DTO.Responses.Analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsightsResponseDTO {
    private String fraudSummary;
    private String financialInsights;
    private List<String> topRecommendations;
}
