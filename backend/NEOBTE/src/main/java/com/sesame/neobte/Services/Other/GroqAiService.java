package com.sesame.neobte.Services.Other;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the Groq chat-completions API (OpenAI-compatible).
 * Uses the free tier — no billing required.
 * Model: llama-3.3-70b-versatile
 */
@Service
@Slf4j
public class GroqAiService {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api-key:}")
    private String apiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String model;

    private final RestClient restClient;

    public GroqAiService() {
        this.restClient = RestClient.create();
    }

    /**
     * Send a user prompt and return the AI response text.
     * Returns a fallback message if the API key is missing or the call fails.
     */
    public String chat(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GROQ] GROQ_API_KEY is not configured — returning fallback.");
            return "AI insights unavailable: please configure GROQ_API_KEY.";
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user",   "content", userPrompt)
                    ),
                    "temperature", 0.4,
                    "max_tokens",  1024
            );

            GroqResponse response = restClient.post()
                    .uri(GROQ_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }
            return "No response from AI.";

        } catch (Exception e) {
            log.error("[GROQ] API call failed: {}", e.getMessage());
            return "AI insights temporarily unavailable.";
        }
    }

    /**
     * Multi-turn overload — send a fully built messages list (system + history + user).
     * Used by the chatbot where conversation history must be preserved.
     */
    public String chatWithHistory(List<Map<String, Object>> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[GROQ] GROQ_API_KEY is not configured — returning fallback.");
            return "AI assistant unavailable: please configure GROQ_API_KEY.";
        }
        try {
            Map<String, Object> body = Map.of(
                    "model",       model,
                    "messages",    messages,
                    "temperature", 0.6,
                    "max_tokens",  768
            );
            GroqResponse response = restClient.post()
                    .uri(GROQ_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(GroqResponse.class);
            if (response != null && response.choices() != null && !response.choices().isEmpty()) {
                return response.choices().get(0).message().content();
            }
            return "No response from AI.";
        } catch (Exception e) {
            log.error("[GROQ] chatWithHistory error: {}", e.getMessage());
            return "AI assistant temporarily unavailable.";
        }
    }

    // ── Internal response model ───────────────────────────────────────────────

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroqResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(Message message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String role, String content) {}
}
