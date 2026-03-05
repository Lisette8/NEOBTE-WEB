package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Support.SupportCreateDTO;
import com.sesame.neobte.DTO.Responses.Support.SupportResponseDTO;
import com.sesame.neobte.Entities.Support;
import com.sesame.neobte.Entities.SupportStatus;
import com.sesame.neobte.Entities.Utilisateur;
import com.sesame.neobte.Repositories.ISupportRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class SupportServiceImpl implements SupportService {

    private ISupportRepository supportRepository;
    private IUtilisateurRepository utilisateurRepository;


    @Override
    public List<SupportResponseDTO> getMyTickets(Long userId) {
        List<Support> tickets = supportRepository.findByUtilisateurIdUtilisateur(userId);

        return tickets.stream()
                .map(this::mapToResponseDTO)
                .toList();
    }


    @Override
    public List<Support> getAllTickets() {
        return supportRepository.findAll();
    }


    //crud support

    @Override
    public SupportResponseDTO createTicket(Long userId, SupportCreateDTO dto) {

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Support support = new Support();
        support.setSujet(dto.getSujet());
        support.setMessage(dto.getMessage());
        support.setStatus(SupportStatus.OPEN);
        support.setUtilisateur(user);
        support.setDateCreation(LocalDateTime.now());

        supportRepository.save(support);

        return mapToResponseDTO(support);
    }


    @Override
    public Support updateStatus(Long id, String response, String status) {

        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        support.setReponseAdmin(response);
        support.setStatus(SupportStatus.valueOf(status.toUpperCase()));

        return supportRepository.save(support);
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
                support.getDateCreation()
        );
    }
}
