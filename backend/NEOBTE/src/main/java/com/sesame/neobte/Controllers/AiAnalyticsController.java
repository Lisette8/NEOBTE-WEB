package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.AI.ChatRequestDTO;
import com.sesame.neobte.DTO.Responses.Analytics.AiInsightsResponseDTO;
import com.sesame.neobte.DTO.Responses.Analytics.AnalyticsResponseDTO;
import com.sesame.neobte.DTO.Responses.Analytics.ChatResponseDTO;
import com.sesame.neobte.Services.AiAnalyticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/v1/admin/ai")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AiAnalyticsController {

    private final AiAnalyticsService aiAnalyticsService;

    /** Returns all chart datasets + key metrics. No AI call — fast. */
    @GetMapping("/analytics")
    public AnalyticsResponseDTO getAnalytics() {
        return aiAnalyticsService.getAnalytics();
    }

    /**
     * Triggers Groq AI analysis. Slightly slower due to external API call.
     * Frontend should call this only on explicit user action to respect free-tier limits.
     */
    @GetMapping("/insights")
    public AiInsightsResponseDTO getInsights() {
        return aiAnalyticsService.getAiInsights();
    }

    /**
     * Admin chatbot — single turn with full conversation history.
     * Context is automatically enriched with live analytics data.
     */
    @PostMapping("/chat")
    public ChatResponseDTO chat(@Valid @RequestBody ChatRequestDTO req) {
        return aiAnalyticsService.chat(req.getMessage(), req.getHistory());
    }
}
