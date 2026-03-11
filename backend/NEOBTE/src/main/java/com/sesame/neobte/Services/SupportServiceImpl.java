package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Support.SupportCreateDTO;
import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Class.Support;
import com.sesame.neobte.Entities.Enumeration.SupportStatus;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Exceptions.ResourceNotFoundException;
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
        List<Support> tickets = supportRepository.findByUtilisateurIdUtilisateur(userId);

        return tickets.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    @Override
    public List<SupportResponseDTO> getAllTickets() {
        List<Support> tickets = supportRepository.findAll();

        return tickets.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    //crud support

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

        supportRepository.save(support);

        //send notif
        messagingTemplate.convertAndSend("/topic/support", "New support ticket from: " + user.getEmail());

        return mapToResponseDTO(support);
    }


    @Override
    public Support updateStatus(Long id, String response, String status) {

        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        support.setReponseAdmin(response);
        support.setStatus(SupportStatus.valueOf(status.toUpperCase()));

        Support saved = supportRepository.save(support);

        // send email to client
        String clientEmail = support.getUtilisateur().getEmail();

        String subject = "Response to your support ticket";

        String message =
                "Hello,\n\n" +
                        "Our support team replied to your ticket:\n\n" +
                        "Subject: " + support.getSujet() + "\n\n" +
                        "Admin response:\n" +
                        response +
                        "\n\nBest regards,\nSupport Team";

        emailService.sendSupportResponseEmail(clientEmail, subject, message);

        return saved;
    }


    @Override
    public void deleteTicket(Long id) {
        supportRepository.deleteById(id);
    }


    //private functions
    private SupportResponseDTO mapToResponseDTO(Support support) {
        return new SupportResponseDTO(
                support.getIdSupport(),
                support.getSujet(),
                support.getMessage(),
                support.getReponseAdmin(),
                support.getStatus().name(),
                support.getDateCreation(),
                support.getUtilisateur().getEmail()
        );
    }
}