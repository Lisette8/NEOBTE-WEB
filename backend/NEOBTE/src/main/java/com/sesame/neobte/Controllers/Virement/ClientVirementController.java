package com.sesame.neobte.Controllers.Virement;

import com.sesame.neobte.DTO.Requests.Virement.InternalTransferCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Requests.Virement.VirementHistoryFilterDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.TransferConstraintsDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementHistoryPageDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Security.Services.JwtService;
import com.sesame.neobte.Services.VirementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/client/virements")
@AllArgsConstructor
public class ClientVirementController {

    private VirementService virementService;
    private JwtService jwtService;

    @GetMapping("/resolve-recipient")
    public RecipientPreviewDTO resolveRecipient(@RequestParam String identifier, Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return virementService.resolveRecipient(identifier, userId);
    }

    @GetMapping("/constraints")
    public TransferConstraintsDTO constraints(
            @RequestParam(defaultValue = "false") boolean internal,
            Authentication auth
    ) {
        Long userId = (Long) auth.getPrincipal();
        return virementService.getConstraints(userId, internal);
    }

    @PostMapping("/transfer")
    public VirementResponseDTO transfer(
            @Valid @RequestBody VirementCreateDTO dto,
            HttpServletRequest request) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));
        return virementService.effectuerVirement(dto, userId);
    }

    @PostMapping("/transfer-interne")
    public VirementResponseDTO transferInterne(
            @Valid @RequestBody InternalTransferCreateDTO dto,
            HttpServletRequest request
    ) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));
        return virementService.effectuerVirementInterne(dto, userId);
    }

    @GetMapping("/history")
    public List<VirementResponseDTO> history(HttpServletRequest request) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));
        return virementService.getVirementsUtilisateur(userId);
    }

    /**
     * Filtered, paginated, sortable history endpoint.
     * Replaces the raw /history for new clients; /history kept for backward compat.
     *  
     * GET /api/v1/client/virements/history/filter
     *   ?search=Mohamed&period=30d&type=sent&sort=date-desc&page=0&size=20
     */
    @GetMapping("/history/filter")
    public VirementHistoryPageDTO filteredHistory(
            HttpServletRequest request,
            @ModelAttribute VirementHistoryFilterDTO filter
    ) {
        Long userId = jwtService.extractUserId(request.getHeader("Authorization").substring(7));
        return virementService.getFilteredHistory(userId, filter);
    }



}
