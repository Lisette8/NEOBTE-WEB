package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.AI.ChatMessageDTO;
import com.sesame.neobte.DTO.Requests.AI.ChatRequestDTO;
import com.sesame.neobte.DTO.Responses.Analytics.ChatResponseDTO;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import com.sesame.neobte.Services.Other.GroqAiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/v1/client/ai")
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
@RequiredArgsConstructor
public class ClientAiController {

    private static final int FREE_LIMIT    = 10;
    private static final int PREMIUM_LIMIT = 50;

    private final IUtilisateurRepository utilisateurRepository;
    private final IVirementRepository    virementRepository;
    private final GroqAiService          groqAiService;

    /** Returns the caller's plan status + usage for the current month. */
    @GetMapping("/premium/status")
    public Map<String, Object> getPremiumStatus(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        Date monthStart = Date.from(LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
        long used  = virementRepository.countOutgoingSince(userId, monthStart);
        int  limit = user.isPremium() ? PREMIUM_LIMIT : FREE_LIMIT;

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("premium", user.isPremium());
        resp.put("transfersThisMonth", used);
        resp.put("monthlyLimit", limit);
        return resp;
    }

    /**
     * Client AI Chatbot — premium only.
     * Answers finance, banking and BTE-related questions with Groq AI.
     * The system prompt includes general finance/news context; no live external news feed
     * is required — Groq's training data covers macro finance trends.
     */
    @PostMapping("/chat")
    public ChatResponseDTO chat(@Valid @RequestBody ChatRequestDTO req, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        if (!user.isPremium()) {
            throw new BadRequestException(
                "Cette fonctionnalité est réservée aux abonnés Premium. " +
                "Rendez-vous dans l'agence BTE la plus proche pour passer à Premium.");
        }

        String systemPrompt = String.format("""
                Tu es un assistant IA de finances personnelles pour %s, un client de NeoBTE (Banque de Tunisie et des Émirats).
                
                Ton rôle :
                - Répondre aux questions sur la finance personnelle, la banque, le budget, l'épargne, l'investissement et les tendances économiques.
                - Partager des informations générales sur la BTE et le secteur bancaire en Tunisie lorsque c'est demandé.
                - Donner des explications générales sur les marchés, devises et indicateurs économiques.
                - Expliquer les fonctionnalités de NeoBTE et comment les utiliser.
                
                Consignes :
                - Reste professionnel, clair et concis.
                - Précise toujours que tu fournis des informations générales et non un conseil d'investissement personnalisé.
                - Réponds en français.
                - Garde les réponses sous ~200 mots sauf demande explicite de détail.
                """,
                user.getPrenom() + " " + user.getNom());

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (req.getHistory() != null) {
            req.getHistory().forEach(h -> messages.add(Map.of("role", h.getRole(), "content", h.getContent())));
        }
        messages.add(Map.of("role", "user", "content", req.getMessage()));

        String reply = groqAiService.chatWithHistory(messages);
        return new ChatResponseDTO(reply);
    }
}
