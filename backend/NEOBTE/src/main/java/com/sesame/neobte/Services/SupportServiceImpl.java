package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Contact.ContactCreateDTO;
import com.sesame.neobte.DTO.Requests.Support.SupportCreateDTO;
import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Class.Support;
import com.sesame.neobte.Entities.Enumeration.SupportStatus;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ISupportRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class SupportServiceImpl implements SupportService {

    private ISupportRepository supportRepository;
    private IUtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private EmailService emailService;

    @Override
    public List<SupportResponseDTO> getMyTickets(Long userId) {
        return supportRepository.findByUtilisateurIdUtilisateur(userId)
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public List<SupportResponseDTO> getAllTickets() {
        return supportRepository.findAll()
                .stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public SupportResponseDTO createTicket(Long userId, SupportCreateDTO dto) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Support support = new Support();
        support.setSujet(dto.getSujet());
        support.setMessage(dto.getMessage());
        support.setStatus(SupportStatus.OPEN);
        support.setUtilisateur(user);
        support.setDateCreation(LocalDateTime.now());

        Support saved = supportRepository.save(support);
        messagingTemplate.convertAndSend("/topic/support", mapToResponseDTO(saved));
        return mapToResponseDTO(saved);
    }

    @Override
    public SupportResponseDTO createGuestTicket(ContactCreateDTO dto) {
        Support support = new Support();
        support.setSujet(dto.getSujet());
        support.setMessage(dto.getMessage());
        support.setStatus(SupportStatus.OPEN);
        support.setGuestEmail(dto.getEmail());
        support.setGuestName(dto.getNom());
        support.setUtilisateur(null);
        support.setDateCreation(LocalDateTime.now());

        Support saved = supportRepository.save(support);
        // Broadcast to admin WebSocket panel so it appears live
        messagingTemplate.convertAndSend("/topic/support", mapToResponseDTO(saved));
        return mapToResponseDTO(saved);
    }

    @Override
    public SupportResponseDTO updateStatus(Long id, String response, String status) {
        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        support.setReponseAdmin(response);
        support.setStatus(SupportStatus.valueOf(status.toUpperCase()));
        Support saved = supportRepository.save(support);

        // Reply by email — use guest email if no registered user
        String replyTo = support.getUtilisateur() != null
                ? support.getUtilisateur().getEmail()
                : support.getGuestEmail();

        if (replyTo != null && !replyTo.isBlank()) {
            emailService.sendSupportResponseEmail(replyTo, support.getSujet(), response);
        }

        return mapToResponseDTO(saved);
    }

    @Override
    public void deleteTicket(Long id) {
        supportRepository.deleteById(id);
    }

    private SupportResponseDTO mapToResponseDTO(Support support) {
        return SupportResponseDTO.builder()
                .idSupport(support.getIdSupport())
                .sujet(support.getSujet())
                .message(support.getMessage())
                .reponseAdmin(support.getReponseAdmin())
                .status(support.getStatus().name())
                .dateCreation(support.getDateCreation())
                .clientEmail(support.getUtilisateur() != null ? support.getUtilisateur().getEmail() : null)
                .guestEmail(support.getGuestEmail())
                .guestName(support.getGuestName())
                .guest(support.getUtilisateur() == null)
                .build();
    }
}
