package com.sesame.neobte.Controllers.Contact;

import com.sesame.neobte.DTO.Requests.Contact.ContactCreateDTO;
import com.sesame.neobte.Services.SupportService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public/contact")
@AllArgsConstructor
public class PublicContactController {

    private final SupportService supportService;

    /**
     * POST /api/v1/public/contact
     * Public endpoint — no authentication required.
     * Used by the landing page contact form for unauthenticated visitors.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> submit(@Valid @RequestBody ContactCreateDTO dto) {
        supportService.createGuestTicket(dto);
        return ResponseEntity.ok(Map.of("message",
                "Votre message a bien été envoyé. Notre équipe vous répondra à " + dto.getEmail() + " dans les plus brefs délais."));
    }
}