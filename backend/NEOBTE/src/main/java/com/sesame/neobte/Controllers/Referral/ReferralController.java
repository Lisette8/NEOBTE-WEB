package com.sesame.neobte.Controllers.Referral;

import com.sesame.neobte.DTO.Responses.Referral.ReferralDashboardDTO;
import com.sesame.neobte.Entities.Class.ReferralReward;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Services.Other.ReferralService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/referral")
@PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
@RequiredArgsConstructor
public class ReferralController {

    private final ReferralService referralService;
    private final IUtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;

    @Value("${neobte.frontend.base-url}")
    private String frontendBaseUrl;

    @GetMapping
    public ReferralDashboardDTO getDashboard(HttpServletRequest request) {
        Long userId = extractUserId(request);
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String code = referralService.ensureReferralCode(user);
        String link = frontendBaseUrl + "/auth-view?ref=" + code;

        List<ReferralReward> referrals = referralService.getReferralsForUser(userId);
        List<ReferralDashboardDTO.ReferralEntryDTO> entries = referrals.stream()
                .map(r -> ReferralDashboardDTO.ReferralEntryDTO.builder()
                        .id(r.getId())
                        .referredName(r.getReferred().getPrenom() + " " + r.getReferred().getNom())
                        .referredEmail(r.getReferred().getEmail())
                        .dateReferral(r.getDateReferral())
                        .rewarded(r.isRewarded())
                        .build())
                .toList();

        return ReferralDashboardDTO.builder()
                .referralCode(code)
                .referralLink(link)
                .totalReferrals(referrals.size())
                .premium(user.isPremium())
                .premiumExpiresAt(user.getPremiumExpiresAt())
                .referrals(entries)
                .build();
    }

    /**
     * POST /api/v1/client/referral/apply-code
     * Body: { "referralCode": "XXXX" }
     * Called by the frontend right after successful registration.
     * Returns 200 with a success message, or 400 with a user-facing error.
     */
    @PostMapping("/apply-code")
    public ResponseEntity<Map<String, String>> applyCode(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {

        Long userId = extractUserId(request);
        String message = referralService.applyReferralCode(userId, body.get("referralCode"));
        return ResponseEntity.ok(Map.of("message", message));
    }

    private Long extractUserId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer "))
            throw new ResourceNotFoundException("No token");
        return jwtService.extractUserId(auth.substring(7));
    }
}