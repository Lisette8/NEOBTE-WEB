package com.sesame.neobte.Services;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
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
    private final NotificationService notificationService;

    // email -> { code, expiry }
    private final Map<String, CodeEntry> codeStore = new ConcurrentHashMap<>();
    // resetToken -> email (valid only after code is verified)
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    private static final int CODE_EXPIRY_SECONDS  = 900;  // 15 minutes
    private static final int TOKEN_EXPIRY_SECONDS = 600;  // 10 minutes
    private static final int MAX_ATTEMPTS         = 5;

    // ── Step 1: Send code ──────────────────────────────────────────────────
    public void sendResetCode(String email) {
        String normalizedEmail = normalizeEmail(email);
        Utilisateur user = utilisateurRepository.findByEmail(normalizedEmail);
        if (user == null) {
            log.warn("Password reset requested for unknown email: {}", normalizedEmail);
            throw new BadRequestException("Aucun compte n'est associé à cet e-mail.");
        }

        String code = generateCode();
        codeStore.put(normalizedEmail, new CodeEntry(code, Instant.now().plusSeconds(CODE_EXPIRY_SECONDS), 0));

        try {
            emailService.sendPasswordResetEmail(normalizedEmail, user.getPrenom(), code);
            log.info("Password reset code sent to {}", normalizedEmail);
        } catch (Exception e) {
            log.error("Failed to send reset code to {}: {}", normalizedEmail, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'e-mail de vérification. Veuillez réessayer.");
        }
    }

    // ── Step 2: Verify code → return one-time reset token ─────────────────
    public String verifyCode(String email, String code) {
        String normalizedEmail = normalizeEmail(email);
        CodeEntry entry = codeStore.get(normalizedEmail);

        if (entry == null)
            throw new BadRequestException("Aucune demande de réinitialisation trouvée. Veuillez demander un nouveau code.");

        if (Instant.now().isAfter(entry.expiry())) {
            codeStore.remove(normalizedEmail);
            throw new BadRequestException("Ce code a expiré. Veuillez en demander un nouveau.");
        }

        if (entry.attempts() >= MAX_ATTEMPTS) {
            codeStore.remove(normalizedEmail);
            throw new BadRequestException("Trop de tentatives incorrectes. Veuillez demander un nouveau code.");
        }

        if (!entry.code().equals(code.trim())) {
            // Increment attempt counter
            codeStore.put(normalizedEmail, new CodeEntry(entry.code(), entry.expiry(), entry.attempts() + 1));
            int remaining = MAX_ATTEMPTS - entry.attempts() - 1;
            throw new BadRequestException("Code incorrect. Il vous reste " + remaining + " tentative" + (remaining == 1 ? "" : "s") + ".");
        }

        // Code is valid — remove it and issue a one-time reset token
        codeStore.remove(normalizedEmail);
        String resetToken = UUID.randomUUID().toString();
        tokenStore.put(resetToken, new TokenEntry(normalizedEmail, Instant.now().plusSeconds(TOKEN_EXPIRY_SECONDS)));
        log.info("Password reset code verified for {}", normalizedEmail);
        return resetToken;
    }

    // ── Step 3: Reset password with token ─────────────────────────────────
    public void resetPassword(String resetToken, String newPassword) {
        TokenEntry entry = tokenStore.get(resetToken);

        if (entry == null)
            throw new BadRequestException("Jeton de réinitialisation invalide ou expiré. Veuillez recommencer.");

        if (Instant.now().isAfter(entry.expiry())) {
            tokenStore.remove(resetToken);
            throw new BadRequestException("Le jeton de réinitialisation a expiré. Veuillez recommencer.");
        }

        Utilisateur user = utilisateurRepository.findByEmail(entry.email());
        if (user == null)
            throw new BadRequestException("Utilisateur introuvable.");

        user.setMotDePasse(passwordEncoder.encode(newPassword));
        user.setDateDernierChangementMotDePasse(LocalDateTime.now());
        utilisateurRepository.save(user);

        notificationService.notifyUser(
                user.getIdUtilisateur(),
                NotificationType.PASSWORD_CHANGED,
                "Mot de passe réinitialisé",
                "Votre mot de passe a été réinitialisé avec succès. Si vous n'êtes pas à l'origine de cette action, contactez le support immédiatement.",
                "/settings-view"
        );

        // Invalidate token immediately after use
        tokenStore.remove(resetToken);
        log.info("Password successfully reset for {}", entry.email());
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private static String normalizeEmail(String email) {
        return (email == null ? "" : email.trim().toLowerCase());
    }

    private record CodeEntry(String code, Instant expiry, int attempts) {}
    private record TokenEntry(String email, Instant expiry) {}
}
