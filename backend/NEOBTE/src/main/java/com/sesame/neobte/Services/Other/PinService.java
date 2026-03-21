package com.sesame.neobte.Services.Other;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.UnauthorizedException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Security.Services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PinService {
    private final IUtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private static final int PIN_TEMP_TOKEN_MINUTES = 5;
    private static final int BYPASS_CODE_MINUTES    = 15;
    private static final int MAX_PIN_ATTEMPTS       = 5;

    // userId -> { bypassCode, expiry, attempts }
    private final Map<Long, BypassEntry> bypassStore = new ConcurrentHashMap<>();
    // userId -> failed PIN attempt count (reset on success)
    private final Map<Long, Integer> pinAttempts = new ConcurrentHashMap<>();

    // ── Called by AuthServiceImpl after password check ─────────────────────

    /**
     * Issues a short-lived pinTempToken saved on the user row.
     * Returns the token so the frontend can pass it back at /verify-pin.
     */
    @Transactional
    public String issuePinTempToken(Utilisateur user) {
        String token = UUID.randomUUID().toString();
        user.setPinTempToken(token);
        user.setPinTempTokenExpiry(LocalDateTime.now().plusMinutes(PIN_TEMP_TOKEN_MINUTES));
        utilisateurRepository.save(user);
        return token;
    }

    // ── /verify-pin ────────────────────────────────────────────────────────

    /**
     * Validates the pinTempToken + PIN, returns a full JWT on success.
     */
    @Transactional
    public String verifyPin(String pinTempToken, String pin) {
        Utilisateur user = utilisateurRepository.findByPinTempToken(pinTempToken)
                .orElseThrow(() -> new UnauthorizedException("Session expirée. Veuillez vous reconnecter."));

        if (user.getPinTempTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPinTempTokenExpiry())) {
            clearPinTempToken(user);
            throw new UnauthorizedException("Session expirée. Veuillez vous reconnecter.");
        }

        Long userId = user.getIdUtilisateur();
        int attempts = pinAttempts.getOrDefault(userId, 0);

        if (attempts >= MAX_PIN_ATTEMPTS) {
            throw new UnauthorizedException("Trop de tentatives. Veuillez utiliser l'option 'PIN oublié'.");
        }

        if (!passwordEncoder.matches(pin, user.getPinCode())) {
            pinAttempts.put(userId, attempts + 1);
            int remaining = MAX_PIN_ATTEMPTS - attempts - 1;
            throw new UnauthorizedException("PIN incorrect. " + remaining + " tentative" + (remaining == 1 ? "" : "s") + " restante" + (remaining == 1 ? "" : "s") + ".");
        }

        // Success — clear temp token and attempt counter
        pinAttempts.remove(userId);
        clearPinTempToken(user);

        return jwtService.generateToken(user.getIdUtilisateur(), user.getRole().toString());
    }

    // ── Forgot PIN flow ────────────────────────────────────────────────────

    /**
     * Step 1: send a bypass code by email (user must have a valid pinTempToken = be mid-login).
     */
    @Transactional
    public void sendPinBypassCode(String pinTempToken) {
        Utilisateur user = utilisateurRepository.findByPinTempToken(pinTempToken)
                .orElseThrow(() -> new UnauthorizedException("Session expirée. Veuillez vous reconnecter."));

        if (user.getPinTempTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPinTempTokenExpiry())) {
            clearPinTempToken(user);
            throw new UnauthorizedException("Session expirée. Veuillez vous reconnecter.");
        }

        String code = generateCode();
        bypassStore.put(user.getIdUtilisateur(),
                new BypassEntry(code, Instant.now().plusSeconds(BYPASS_CODE_MINUTES * 60L), 0));

        emailService.sendPinBypassEmail(user.getEmail(), user.getPrenom(), code);
        log.info("[PIN] Bypass code sent to {}", user.getEmail());
    }

    /**
     * Step 2: verify bypass code → return full JWT, reset PIN.
     */
    @Transactional
    public String verifyPinBypassCode(String pinTempToken, String code) {
        Utilisateur user = utilisateurRepository.findByPinTempToken(pinTempToken)
                .orElseThrow(() -> new UnauthorizedException("Session expirée. Veuillez vous reconnecter."));

        if (user.getPinTempTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getPinTempTokenExpiry())) {
            clearPinTempToken(user);
            throw new UnauthorizedException("Session expirée. Veuillez vous reconnecter.");
        }

        Long userId = user.getIdUtilisateur();
        BypassEntry entry = bypassStore.get(userId);

        if (entry == null)
            throw new BadRequestException("Aucun code envoyé. Veuillez demander un nouveau code.");

        if (Instant.now().isAfter(entry.expiry())) {
            bypassStore.remove(userId);
            throw new BadRequestException("Le code a expiré. Veuillez en demander un nouveau.");
        }

        if (entry.attempts() >= MAX_PIN_ATTEMPTS) {
            bypassStore.remove(userId);
            throw new BadRequestException("Trop de tentatives incorrectes.");
        }

        if (!entry.code().equals(code.trim())) {
            bypassStore.put(userId, new BypassEntry(entry.code(), entry.expiry(), entry.attempts() + 1));
            int remaining = MAX_PIN_ATTEMPTS - entry.attempts() - 1;
            throw new BadRequestException("Code incorrect. " + remaining + " tentative" + (remaining == 1 ? "" : "s") + " restante" + (remaining == 1 ? "" : "s") + ".");
        }

        // Success — disable PIN (user must re-set it), clear everything
        bypassStore.remove(userId);
        pinAttempts.remove(userId);
        user.setPinCode(null);
        user.setPinEnabled(false);
        clearPinTempToken(user);
        utilisateurRepository.save(user);

        log.info("[PIN] Bypass successful for {} — PIN disabled, user must re-set it.", user.getEmail());
        return jwtService.generateToken(userId, user.getRole().toString());
    }

    // ── Settings endpoints (authenticated) ────────────────────────────────

    @Transactional
    public void enablePin(Long userId, String pin) {
        if (pin == null || !pin.matches("\\d{4,6}"))
            throw new BadRequestException("Le PIN doit contenir entre 4 et 6 chiffres.");

        Utilisateur user = getUser(userId);
        user.setPinCode(passwordEncoder.encode(pin));
        user.setPinEnabled(true);
        utilisateurRepository.save(user);
        log.info("[PIN] Enabled for user {}", userId);
    }

    @Transactional
    public void disablePin(Long userId, String pin) {
        Utilisateur user = getUser(userId);
        if (!user.isPinEnabled())
            throw new BadRequestException("Le PIN n'est pas activé.");
        if (!passwordEncoder.matches(pin, user.getPinCode()))
            throw new UnauthorizedException("PIN incorrect.");

        user.setPinCode(null);
        user.setPinEnabled(false);
        utilisateurRepository.save(user);
        log.info("[PIN] Disabled for user {}", userId);
    }

    @Transactional
    public void changePin(Long userId, String oldPin, String newPin) {
        if (newPin == null || !newPin.matches("\\d{4,6}"))
            throw new BadRequestException("Le nouveau PIN doit contenir entre 4 et 6 chiffres.");

        Utilisateur user = getUser(userId);
        if (!user.isPinEnabled())
            throw new BadRequestException("Le PIN n'est pas activé.");
        if (!passwordEncoder.matches(oldPin, user.getPinCode()))
            throw new UnauthorizedException("Ancien PIN incorrect.");

        user.setPinCode(passwordEncoder.encode(newPin));
        utilisateurRepository.save(user);
        log.info("[PIN] Changed for user {}", userId);
    }

    /**
     * Send a bypass code by email from settings (for users who forgot their PIN but are logged in).
     */
    @Transactional
    public void sendPinResetCodeFromSettings(Long userId) {
        Utilisateur user = getUser(userId);
        if (!user.isPinEnabled())
            throw new BadRequestException("Le PIN n'est pas activé.");

        String code = generateCode();
        bypassStore.put(userId,
                new BypassEntry(code, Instant.now().plusSeconds(BYPASS_CODE_MINUTES * 60L), 0));

        emailService.sendPinBypassEmail(user.getEmail(), user.getPrenom(), code);
        log.info("[PIN] Reset code sent from settings for user {}", userId);
    }

    /**
     * Verify bypass code from settings → disable PIN so user can set a new one.
     */
    @Transactional
    public void verifyPinResetCodeFromSettings(Long userId, String code) {
        Utilisateur user = getUser(userId);
        BypassEntry entry = bypassStore.get(userId);

        if (entry == null)
            throw new BadRequestException("Aucun code envoyé. Veuillez demander un nouveau code.");

        if (Instant.now().isAfter(entry.expiry())) {
            bypassStore.remove(userId);
            throw new BadRequestException("Le code a expiré.");
        }

        if (entry.attempts() >= MAX_PIN_ATTEMPTS) {
            bypassStore.remove(userId);
            throw new BadRequestException("Trop de tentatives incorrectes.");
        }

        if (!entry.code().equals(code.trim())) {
            bypassStore.put(userId, new BypassEntry(entry.code(), entry.expiry(), entry.attempts() + 1));
            int remaining = MAX_PIN_ATTEMPTS - entry.attempts() - 1;
            throw new BadRequestException("Code incorrect. " + remaining + " tentative" + (remaining == 1 ? "" : "s") + " restante" + (remaining == 1 ? "" : "s") + ".");
        }

        bypassStore.remove(userId);
        user.setPinCode(null);
        user.setPinEnabled(false);
        utilisateurRepository.save(user);
        log.info("[PIN] Reset via settings for user {}", userId);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private void clearPinTempToken(Utilisateur user) {
        user.setPinTempToken(null);
        user.setPinTempTokenExpiry(null);
        utilisateurRepository.save(user);
    }

    private Utilisateur getUser(Long userId) {
        return utilisateurRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable."));
    }

    private String generateCode() {
        return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private record BypassEntry(String code, Instant expiry, int attempts) {}
}
