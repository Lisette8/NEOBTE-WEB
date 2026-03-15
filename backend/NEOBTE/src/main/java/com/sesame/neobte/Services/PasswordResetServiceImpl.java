package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final IUtilisateurRepository utilisateurRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // email -> { code, expiry }
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    // resetToken -> email (valid only after code is verified)
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRY_SECONDS  = 900;  // 15 minutes
    private static final int TOKEN_EXPIRY_SECONDS = 600;  // 10 minutes
    private static final int MAX_ATTEMPTS         = 5;

    // ── Step 1: Send code ──────────────────────────────────────────────────
    public void sendResetCode(String email) {
        Utilisateur user = utilisateurRepository.findByEmail(email);
        // Always respond with success even if email not found — prevents user enumeration
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", email);
            return;
        }

        String code = generateCode();
        codeStore.put(email, new CodeEntry(code, Instant.now().plusSeconds(CODE_EXPIRY_SECONDS), 0));

        try {
            emailService.sendPasswordResetEmail(email, user.getPrenom(), code);
            log.info("Password reset code sent to {}", email);
        } catch (Exception e) {
            log.error("Failed to send reset code to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send verification email. Please try again.");
        }
    }

    // ── Step 2: Verify code → return one-time reset token ─────────────────
    public String verifyCode(String email, String code) {
        CodeEntry entry = codeStore.get(email);

        if (entry == null)
            throw new BadRequestException("No reset request found for this email. Please request a new code.");

        if (Instant.now().isAfter(entry.expiry())) {
            codeStore.remove(email);
            throw new BadRequestException("This code has expired. Please request a new one.");
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            codeStore.remove(email);
            throw new BadRequestException("Too many incorrect attempts. Please request a new code.");
        }

        if (!entry.code().equals(code.trim())) {
            // Increment attempt counter
            codeStore.put(email, new CodeEntry(entry.code(), entry.expiry(), entry.attempts() + 1));
            int remaining = MAX_ATTEMPTS - entry.attempts() - 1;
            throw new BadRequestException("Incorrect code. " + remaining + " attempt" + (remaining == 1 ? "" : "s") + " remaining.");
        }

        // Code is valid — remove it and issue a one-time reset token
        codeStore.remove(email);
        String resetToken = UUID.randomUUID().toString();
        tokenStore.put(resetToken, new TokenEntry(email, Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS)));
        log.info("Password reset code verified for {}", email);
        return resetToken;
    }

    // ── Step 3: Reset password with token ─────────────────────────────────
    public void resetPassword(String resetToken, String newPassword) {
        TokenEntry entry = tokenStore.get(resetToken);

        if (entry == null)
            throw new BadRequestException("Invalid or expired reset token. Please start over.");

        if (Instant.now().isAfter(entry.expiry())) {
            tokenStore.remove(resetToken);
            throw new BadRequestException("Reset token has expired. Please start over.");
        }

        Utilisateur user = utilisateurRepository.findByEmail(entry.email());
        if (user == null)
            throw new BadRequestException("User not found.");

        user.setMotDePasse(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(user);

        // Invalidate token immediately after use
        tokenStore.remove(resetToken);
        log.info("Password successfully reset for {}", entry.email());
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private record CodeEntry(String code, Instant expiry, int attempts) {}
    private record TokenEntry(String email, Instant expiry) {}
}