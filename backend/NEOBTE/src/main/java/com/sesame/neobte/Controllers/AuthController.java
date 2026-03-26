package com.sesame.neobte.Controllers;

import com.sesame.neobte.DTO.Requests.Auth.*;
import com.sesame.neobte.DTO.Responses.Auth.AuthResponse;
import com.sesame.neobte.Services.AuthService;
import com.sesame.neobte.Services.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        authService.logout(request);
        return "Déconnexion réussie.";
    }


    // Step 1: request code
    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendResetCode(request.getEmail());
        return Map.of("message", "Un code de vérification a été envoyé à votre adresse e-mail.");
    }

    // Step 2: verify code → get reset token
    @PostMapping("/verify-reset-code")
    public Map<String, String> verifyResetCode(@Valid @RequestBody VerifyResetCodeRequest request) {
        String resetToken = passwordResetService.verifyCode(request.getEmail(), request.getCode());
        return Map.of("resetToken", resetToken);
    }

    // Step 3: set new password
    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getResetToken(), request.getNewPassword());
        return Map.of("message", "Mot de passe mis à jour. Vous pouvez maintenant vous connecter.");
    }
}
