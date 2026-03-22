package com.sesame.neobte.Controllers.Support;

import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Class.Support;
import com.sesame.neobte.Services.Other.GroqAiService;
import com.sesame.neobte.Services.SupportService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.*;

@AllArgsConstructor
@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/support")
public class AdminSupportController {

    private SupportService supportService;
    private GroqAiService groqAiService;

    @GetMapping("/all")
    public List<SupportResponseDTO> getAllTickets() {
        return supportService.getAllTickets();
    }

    @PutMapping("/update/{id}")
    public SupportResponseDTO update(
            @PathVariable Long id,
            @RequestParam String response,
            @RequestParam String status
    ) {
        return supportService.updateStatus(id, response, status);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id) {
        supportService.deleteTicket(id);
    }

    /**
     * POST /api/v1/admin/support/ai-suggest
     * Body: { sujet, message, guestName? }
     * Returns an AI-generated response suggestion for the given ticket.
     */
    @PostMapping("/ai-suggest")
    public ResponseEntity<Map<String, String>> aiSuggest(@RequestBody Map<String, String> body) {
        String sujet     = body.getOrDefault("sujet", "");
        String message   = body.getOrDefault("message", "");
        String senderName = body.getOrDefault("senderName", "le client");

        String systemPrompt =
                "Tu es un conseiller bancaire professionnel de NEO BTE (Banque de Tunisie et des Émirats). " +
                        "Tu rédiges des réponses de support client courtes, claires, professionnelles et bienveillantes en français. " +
                        "Réponds directement au problème posé. " +
                        "Ne génère que la réponse à envoyer au client, sans titre ni commentaire supplémentaire.";

        String userPrompt = String.format(
                "Le client \"%s\" a soumis un ticket de support.\n\nSujet : %s\n\nMessage : %s\n\n" +
                        "Rédige une réponse professionnelle et concise pour ce ticket.",
                senderName, sujet, message
        );

        String suggestion = groqAiService.chat(systemPrompt, userPrompt);
        return ResponseEntity.ok(Map.of("suggestion", suggestion));
    }
}

