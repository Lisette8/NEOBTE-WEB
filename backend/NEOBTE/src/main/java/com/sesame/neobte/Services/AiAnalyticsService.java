package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.AI.ChatMessageDTO;
import com.sesame.neobte.DTO.Responses.Analytics.AiInsightsResponseDTO;
import com.sesame.neobte.DTO.Responses.Analytics.AnalyticsResponseDTO;
import com.sesame.neobte.DTO.Responses.Analytics.ChatResponseDTO;

import java.util.List;

public interface AiAnalyticsService {
    AnalyticsResponseDTO getAnalytics();
    AiInsightsResponseDTO getAiInsights();
    ChatResponseDTO chat(String message, List<ChatMessageDTO> history);
}
