package com.sesame.neobte.Controllers.Market;

import com.sesame.neobte.DTO.Responses.Market.MarketRatesResponseDTO;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.MarketRatesService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/client/market")
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
@RequiredArgsConstructor
public class ClientMarketController {

    private final IUtilisateurRepository utilisateurRepository;
    private final MarketRatesService marketRatesService;

    @GetMapping("/rates")
    public MarketRatesResponseDTO getRates(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable"));

        if (!user.isPremium()) {
            throw new BadRequestException(
                    "Cette fonctionnalité est réservée aux abonnés Premium. " +
                            "Rendez-vous dans l'agence BTE la plus proche pour passer à Premium."
            );
        }
        return marketRatesService.getRates();
    }
}

