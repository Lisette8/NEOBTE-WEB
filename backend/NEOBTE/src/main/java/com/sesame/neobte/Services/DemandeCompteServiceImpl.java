package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.DemandeCompte.AdminDemandeDecisionDTO;
import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.DemandeCompte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IDemandeCompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.EmailService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DemandeCompteServiceImpl implements DemandeCompteService {

    private final IDemandeCompteRepository demandeRepository;
    private final ICompteRepository compteRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final EmailService emailService;



    @Override
    @Transactional
    public DemandeCompteResponseDTO submitDemande(DemandeCompteCreateDTO dto) {

        Utilisateur user = utilisateurRepository.findById(dto.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Idempotency guard: si le client possede deja d'une demande active...
        boolean alreadyPending = demandeRepository
                .existsByUtilisateur_IdUtilisateurAndTypeCompteAndStatutDemande(
                        dto.getUtilisateurId(), dto.getTypeCompte(), StatutDemande.EN_ATTENTE);

        if (alreadyPending) {
            throw new BadRequestException(
                    "You already have a pending request for a " + dto.getTypeCompte() + " account. " +
                            "Please wait for the admin to process it before submitting a new one.");
        }

        DemandeCompte demande = new DemandeCompte();
        demande.setTypeCompte(dto.getTypeCompte());
        demande.setMotif(dto.getMotif());
        demande.setUtilisateur(user);
        demande.setStatutDemande(StatutDemande.EN_ATTENTE);

        DemandeCompte saved = demandeRepository.save(demande);

        sendConfirmationEmailAsync(user.getEmail(), user.getPrenom(), dto.getTypeCompte().name());

        log.info("New account request submitted: demandeId={}, userId={}, type={}",
                saved.getIdDemande(), user.getIdUtilisateur(), dto.getTypeCompte());

        return mapToDTO(saved);
    }


    @Override
    public List<DemandeCompteResponseDTO> getDemandesByUser(Long userId) {
        return demandeRepository
                .findByUtilisateur_IdUtilisateurOrderByDateDemandeDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public List<DemandeCompteResponseDTO> getAllDemandes() {
        return demandeRepository.findAll()
                .stream()
                // Pending first, then by date descending
                .sorted((a, b) -> {
                    if (a.getStatutDemande() == StatutDemande.EN_ATTENTE &&
                            b.getStatutDemande() != StatutDemande.EN_ATTENTE) return -1;
                    if (b.getStatutDemande() == StatutDemande.EN_ATTENTE &&
                            a.getStatutDemande() != StatutDemande.EN_ATTENTE) return 1;
                    return b.getDateDemande().compareTo(a.getDateDemande());
                })
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    public List<DemandeCompteResponseDTO> getDemandesByStatut(String statut) {
        StatutDemande statutDemande;
        try {
            statutDemande = StatutDemande.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statut + ". Valid values: EN_ATTENTE, APPROUVEE, REJETEE");
        }
        return demandeRepository.findByStatutDemandeOrderByDateDemandeAsc(statutDemande)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }


    @Override
    @Transactional
    public DemandeCompteResponseDTO approveDemande(Long demandeId, AdminDemandeDecisionDTO dto) {

        DemandeCompte demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + demandeId));

        if (demande.getStatutDemande() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("This request has already been processed (status: " +
                    demande.getStatutDemande() + ")");
        }

        // Create the actual bank account
        Compte compte = new Compte();
        compte.setSolde(0.0);
        compte.setTypeCompte(demande.getTypeCompte());
        compte.setStatutCompte(StatutCompte.ACTIF);
        compte.setUtilisateur(demande.getUtilisateur());
        Compte savedCompte = compteRepository.save(compte);

        // Update the request
        demande.setStatutDemande(StatutDemande.APPROUVEE);
        demande.setDateDecision(LocalDateTime.now());
        demande.setCommentaireAdmin(dto.getCommentaireAdmin());
        demande.setCompteOuvert(savedCompte);
        demandeRepository.save(demande);

        Utilisateur user = demande.getUtilisateur();
        sendApprovalEmailAsync(user.getEmail(), user.getPrenom(),
                demande.getTypeCompte().name(), savedCompte.getIdCompte());

        log.info("Account request APPROVED: demandeId={}, newCompteId={}, userId={}",
                demandeId, savedCompte.getIdCompte(), user.getIdUtilisateur());

        return mapToDTO(demande);
    }


    @Override
    @Transactional
    public DemandeCompteResponseDTO rejectDemande(Long demandeId, AdminDemandeDecisionDTO dto) {

        DemandeCompte demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + demandeId));

        if (demande.getStatutDemande() != StatutDemande.EN_ATTENTE) {
            throw new BadRequestException("This request has already been processed (status: " +
                    demande.getStatutDemande() + ")");
        }

        if (dto.getCommentaireAdmin() == null || dto.getCommentaireAdmin().isBlank()) {
            throw new BadRequestException("A reason is required when rejecting a request");
        }

        demande.setStatutDemande(StatutDemande.REJETEE);
        demande.setDateDecision(LocalDateTime.now());
        demande.setCommentaireAdmin(dto.getCommentaireAdmin());
        demandeRepository.save(demande);

        Utilisateur user = demande.getUtilisateur();
        sendRejectionEmailAsync(user.getEmail(), user.getPrenom(),
                demande.getTypeCompte().name(), dto.getCommentaireAdmin());

        log.info("Account request REJECTED: demandeId={}, userId={}, reason={}",
                demandeId, user.getIdUtilisateur(), dto.getCommentaireAdmin());

        return mapToDTO(demande);
    }






    //email functions
    @Async
    protected void sendConfirmationEmailAsync(String email, String prenom, String typeCompte) {
        try {
            emailService.sendDemandeConfirmationEmail(email, prenom, typeCompte);
        } catch (Exception e) {
            log.error("Failed to send confirmation email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    protected void sendApprovalEmailAsync(String email, String prenom, String typeCompte, Long compteId) {
        try {
            emailService.sendDemandeApprovalEmail(email, prenom, typeCompte, compteId);
        } catch (Exception e) {
            log.error("Failed to send approval email to {}: {}", email, e.getMessage());
        }
    }

    @Async
    protected void sendRejectionEmailAsync(String email, String prenom, String typeCompte, String reason) {
        try {
            emailService.sendDemandeRejectionEmail(email, prenom, typeCompte, reason);
        } catch (Exception e) {
            log.error("Failed to send rejection email to {}: {}", email, e.getMessage());
        }
    }




    //mappers
    private DemandeCompteResponseDTO mapToDTO(DemandeCompte d) {
        DemandeCompteResponseDTO dto = new DemandeCompteResponseDTO();
        dto.setIdDemande(d.getIdDemande());
        dto.setTypeCompte(d.getTypeCompte());
        dto.setMotif(d.getMotif());
        dto.setStatutDemande(d.getStatutDemande());
        dto.setDateDemande(d.getDateDemande());
        dto.setDateDecision(d.getDateDecision());
        dto.setCommentaireAdmin(d.getCommentaireAdmin());

        if (d.getCompteOuvert() != null) {
            dto.setCompteOuvertId(d.getCompteOuvert().getIdCompte());
        }

        Utilisateur u = d.getUtilisateur();
        if (u != null) {
            dto.setUtilisateurId(u.getIdUtilisateur());
            dto.setUtilisateurNom(u.getNom());
            dto.setUtilisateurPrenom(u.getPrenom());
            dto.setUtilisateurEmail(u.getEmail());
        }

        return dto;
    }
}
