package com.sesame.neobte.Controllers;

import com.sesame.neobte.Config.AccountTypePolicy;
import com.sesame.neobte.DTO.Requests.AI.ChatMessageDTO;
import com.sesame.neobte.DTO.Requests.AI.ChatRequestDTO;
import com.sesame.neobte.DTO.Responses.Analytics.ChatResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.Fraude.IFraudeConfigRepository;
import com.sesame.neobte.Repositories.ICompteRepository;
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

    private final IUtilisateurRepository utilisateurRepository;
    private final IVirementRepository    virementRepository;
    private final ICompteRepository      compteRepository;
    private final IFraudeConfigRepository fraudeConfigRepository;
    private final GroqAiService          groqAiService;

    /**
     * Returns per-account daily/monthly usage against the admin-configured limits.
     * Reads limits from FraudeConfig (the single DB row) so admin changes take effect immediately.
     */
    @GetMapping("/premium/status")
    public Map<String, Object> getPremiumStatus(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        // Load the live admin-configured limits
        FraudeConfig cfg = fraudeConfigRepository.findById(1L).orElse(null);

        Date dayStart   = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date monthStart = Date.from(LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<Compte> activeAccounts = compteRepository
                .findByUtilisateur_IdUtilisateur(userId)
                .stream()
                .filter(c -> c.getStatutCompte() == StatutCompte.ACTIVE)
                .toList();

        List<Map<String, Object>> accountUsages = new ArrayList<>();
        for (Compte c : activeAccounts) {
            TypeCompte type = c.getTypeCompte();
            long usedToday = virementRepository.countOutgoingFromCompteSince(c.getIdCompte(), dayStart);
            long usedMonth = virementRepository.countOutgoingFromCompteSince(c.getIdCompte(), monthStart);

            Map<String, Object> usage = new LinkedHashMap<>();
            usage.put("compteId",         c.getIdCompte());
            usage.put("typeCompte",        type.name());
            usage.put("dailyCountUsed",    usedToday);
            usage.put("dailyCountLimit",   AccountTypePolicy.dailyCountLimit(type, cfg));   // ← uses cfg
            usage.put("monthlyCountUsed",  usedMonth);
            usage.put("monthlyCountLimit", AccountTypePolicy.monthlyCountLimit(type, cfg)); // ← uses cfg
            usage.put("dailyAmountLimit",  AccountTypePolicy.dailyAmountLimit(type, cfg));  // ← uses cfg
            usage.put("maxTransferAmount", AccountTypePolicy.maxTransferAmount(type, cfg)); // ← uses cfg
            usage.put("canSendExternal",   AccountTypePolicy.canSendExternal(type));
            accountUsages.add(usage);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("premium",       user.isPremium());
        resp.put("accountUsages", accountUsages);
        return resp;
    }

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
