package com.sesame.neobte.Security.Services.Fraude;

import com.sesame.neobte.DTO.Requests.Fraude.FraudeConfigUpdateDTO;
import com.sesame.neobte.DTO.Requests.Fraude.FraudeReviewDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeAlerteResponseDTO;
import com.sesame.neobte.DTO.Responses.Fraude.FraudeConfigResponseDTO;
import com.sesame.neobte.Entities.Class.Fraude.FraudeConfig;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Class.Virement;

import java.util.List;

public interface FraudeService {

    /**
     * Pre-flight hard block — called OUTSIDE any open transaction.
     * Throws BadRequestException if the transfer would exceed a hard limit.
     * Does NOT open a new transaction itself (read-only queries only).
     */
    void enforceHardLimits(Long senderUserId, double montant);

    /**
     * Passive async monitoring — called AFTER the transfer commits.
     * Runs in a separate thread so it never touches the caller's connection.
     * Raises alerts for large transfers, suspicious hour, and rapid succession.
     * Never throws.
     */
    void analyzeTransferAsync(Long virementId, Long senderUserId);

    /** Expose current config entity (read-only, no transaction needed) */
    FraudeConfig getConfigEntity();

    // ── Admin ──
    List<FraudeAlerteResponseDTO> getAllAlertes();
    List<FraudeAlerteResponseDTO> getOpenAlertes();
    FraudeAlerteResponseDTO reviewAlerte(Long id, FraudeReviewDTO dto);
    long countOpen();

    // ── Config ──
    FraudeConfigResponseDTO getConfig();
    FraudeConfigResponseDTO updateConfig(FraudeConfigUpdateDTO dto);
}
