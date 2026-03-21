package com.sesame.neobte.Controllers.PIN;

import com.sesame.neobte.Services.Other.PinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PinController {
    private final PinService pinService;

    // ── Public PIN endpoints (mid-login, no JWT yet) ──────────────────────

    /**
     * POST /api/v1/auth/verify-pin
     * Body: { pinTempToken, pin }
     * Returns full JWT on success.
     */
    @PostMapping("/api/v1/auth/verify-pin")
    public ResponseEntity<Map<String, String>> verifyPin(@RequestBody Map<String, String> body) {
        String token = pinService.verifyPin(body.get("pinTempToken"), body.get("pin"));
        return ResponseEntity.ok(Map.of("token", token));
    }

    /**
     * POST /api/v1/auth/pin/forgot/send-code
     * Body: { pinTempToken }
     * Sends a bypass code to the user's email.
     */
    @PostMapping("/api/v1/auth/pin/forgot/send-code")
    public ResponseEntity<Map<String, String>> sendForgotPinCode(@RequestBody Map<String, String> body) {
        pinService.sendPinBypassCode(body.get("pinTempToken"));
        return ResponseEntity.ok(Map.of("message", "Un code de contournement a été envoyé à votre adresse e-mail."));
    }

    /**
     * POST /api/v1/auth/pin/forgot/verify-code
     * Body: { pinTempToken, code }
     * Verifies bypass code → disables PIN + returns full JWT.
     */
    @PostMapping("/api/v1/auth/pin/forgot/verify-code")
    public ResponseEntity<Map<String, String>> verifyForgotPinCode(@RequestBody Map<String, String> body) {
        String token = pinService.verifyPinBypassCode(body.get("pinTempToken"), body.get("code"));
        return ResponseEntity.ok(Map.of("token", token));
    }

    // ── Authenticated PIN settings endpoints ──────────────────────────────

    /**
     * POST /api/v1/client/pin/enable
     * Body: { pin }
     */
    @PostMapping("/api/v1/client/pin/enable")
    public ResponseEntity<Map<String, String>> enablePin(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.enablePin(userId, body.get("pin"));
        return ResponseEntity.ok(Map.of("message", "PIN activé avec succès."));
    }

    /**
     * POST /api/v1/client/pin/disable
     * Body: { pin }
     */
    @PostMapping("/api/v1/client/pin/disable")
    public ResponseEntity<Map<String, String>> disablePin(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.disablePin(userId, body.get("pin"));
        return ResponseEntity.ok(Map.of("message", "PIN désactivé."));
    }

    /**
     * POST /api/v1/client/pin/change
     * Body: { oldPin, newPin }
     */
    @PostMapping("/api/v1/client/pin/change")
    public ResponseEntity<Map<String, String>> changePin(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.changePin(userId, body.get("oldPin"), body.get("newPin"));
        return ResponseEntity.ok(Map.of("message", "PIN modifié avec succès."));
    }

    /**
     * POST /api/v1/client/pin/forgot/send-code
     * For logged-in users who forgot their PIN.
     */
    @PostMapping("/api/v1/client/pin/forgot/send-code")
    public ResponseEntity<Map<String, String>> sendPinResetFromSettings(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.sendPinResetCodeFromSettings(userId);
        return ResponseEntity.ok(Map.of("message", "Un code a été envoyé à votre adresse e-mail."));
    }

    /**
     * POST /api/v1/client/pin/forgot/verify-code
     * Body: { code }
     * Disables PIN so user can re-set it.
     */
    @PostMapping("/api/v1/client/pin/forgot/verify-code")
    public ResponseEntity<Map<String, String>> verifyPinResetFromSettings(
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        pinService.verifyPinResetCodeFromSettings(userId, body.get("code"));
        return ResponseEntity.ok(Map.of("message", "PIN réinitialisé. Vous pouvez maintenant en définir un nouveau."));
    }
}
