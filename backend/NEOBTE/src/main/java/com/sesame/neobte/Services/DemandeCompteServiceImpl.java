package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.DemandeCompte.AdminDemandeDecisionDTO;
import com.sesame.neobte.DTO.Requests.DemandeCompte.DemandeCompteCreateDTO;
import com.sesame.neobte.DTO.Responses.DemandeCompte.DemandeCompteResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.DemandeCompte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IDemandeCompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Services.Other.EmailService;
import org.springframework.transaction.annotation.Transactional;
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
    public DemandeCompteResponseDTO submitDemande(DemandeCompteCreateDTO dto, Long userId) {

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Resolve CIN — use profile value if already set, otherwise require it from the DTO
        String resolvedCin = user.getCin() != null ? user.getCin() : dto.getCin();
        if (resolvedCin == null || resolvedCin.isBlank())
            throw new BadRequestException("CIN est requis pour ouvrir un compte bancaire.");
        if (!resolvedCin.matches("^[0-9]{8}$"))
            throw new BadRequestException("CIN invalide — 8 chiffres requis.");

        // Resolve dateNaissance — same logic
        java.time.LocalDate resolvedDob = user.getDateNaissance() != null
                ? user.getDateNaissance()
                : dto.getDateNaissance();
        if (resolvedDob == null)
            throw new BadRequestException("Date de naissance requise pour ouvrir un compte bancaire.");

        // Resolve adresse — profile first, then DTO
        String resolvedAdresse = (user.getAdresse() != null && !user.getAdresse().isBlank())
                ? user.getAdresse() : dto.getAdresse();

        // Resolve job — profile first, then DTO
        String resolvedJob = (user.getJob() != null && !user.getJob().isBlank())
                ? user.getJob() : dto.getJob();

        // Validate type-specific required fields
        if (dto.getTypeCompte() == TypeCompte.COURANT || dto.getTypeCompte() == TypeCompte.PROFESSIONNEL) {
            if (resolvedAdresse == null || resolvedAdresse.isBlank())
                throw new BadRequestException("Adresse requise pour ce type de compte.");
            if (resolvedJob == null || resolvedJob.isBlank())
                throw new BadRequestException("Profession requise pour ce type de compte.");
        }
        if (dto.getTypeCompte() == TypeCompte.PROFESSIONNEL) {
            if (dto.getNomEntreprise() == null || dto.getNomEntreprise().isBlank())
                throw new BadRequestException("Nom de l'entreprise requis pour un compte professionnel.");
        }

        // Guard: already owns this account type
        if (compteRepository.existsByUtilisateur_IdUtilisateurAndTypeCompte(userId, dto.getTypeCompte()))
            throw new BadRequestException("You already have a " + dto.getTypeCompte() + " account.");

        // Guard: already has a pending request for this type
        if (demandeRepository.existsByUtilisateur_IdUtilisateurAndTypeCompteAndStatutDemande(
                userId, dto.getTypeCompte(), StatutDemande.EN_ATTENTE))
            throw new BadRequestException("You already have a pending request for a " + dto.getTypeCompte() + " account.");

        // Persist any new KYC data onto the user profile
        if (user.getCin() == null) user.setCin(resolvedCin);
        if (user.getDateNaissance() == null) user.setDateNaissance(resolvedDob);
        if ((user.getAdresse() == null || user.getAdresse().isBlank()) && resolvedAdresse != null)
            user.setAdresse(resolvedAdresse);
        if ((user.getJob() == null || user.getJob().isBlank()) && resolvedJob != null)
            user.setJob(resolvedJob);
        utilisateurRepository.save(user);

        DemandeCompte demande = new DemandeCompte();
        demande.setTypeCompte(dto.getTypeCompte());
        demande.setMotif(dto.getMotif());
        demande.setUtilisateur(user);
        demande.setStatutDemande(StatutDemande.EN_ATTENTE);

        DemandeCompte saved = demandeRepository.save(demande);
        sendConfirmationEmailAsync(user.getEmail(), user.getPrenom(), dto.getTypeCompte().name());
        log.info("New account request: demandeId={}, userId={}, type={}", saved.getIdDemande(), userId, dto.getTypeCompte());
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
        return demandeRepository.findAllByOrderByStatutDemandeAscDateDemandeDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    public List<DemandeCompteResponseDTO> getDemandesByStatut(String statut) {
        StatutDemande statutDemande;
        try {
            statutDemande = StatutDemande.valueOf(statut.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + statut);
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

        if (demande.getStatutDemande() != StatutDemande.EN_ATTENTE)
            throw new BadRequestException("This request has already been processed");

        Compte compte = new Compte();
        compte.setSolde(0.0);
        compte.setTypeCompte(demande.getTypeCompte());
        compte.setStatutCompte(StatutCompte.ACTIVE);
        compte.setUtilisateur(demande.getUtilisateur());
        Compte savedCompte = compteRepository.save(compte);

        demande.setStatutDemande(StatutDemande.APPROUVEE);
        demande.setDateDecision(LocalDateTime.now());
        demande.setCommentaireAdmin(dto.getCommentaireAdmin());
        demande.setCompteOuvert(savedCompte);
        demandeRepository.save(demande);

        Utilisateur user = demande.getUtilisateur();
        sendApprovalEmailAsync(user.getEmail(), user.getPrenom(),
                demande.getTypeCompte().name(), savedCompte.getIdCompte());

        log.info("Account request APPROVED: demandeId={}, newCompteId={}", demandeId, savedCompte.getIdCompte());

        return mapToDTO(demande);
    }

    @Override
    @Transactional
    public DemandeCompteResponseDTO rejectDemande(Long demandeId, AdminDemandeDecisionDTO dto) {
        DemandeCompte demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + demandeId));

        if (demande.getStatutDemande() != StatutDemande.EN_ATTENTE)
            throw new BadRequestException("This request has already been processed");

        if (dto.getCommentaireAdmin() == null || dto.getCommentaireAdmin().isBlank())
            throw new BadRequestException("A reason is required when rejecting a request");

        demande.setStatutDemande(StatutDemande.REJETEE);
        demande.setDateDecision(LocalDateTime.now());
        demande.setCommentaireAdmin(dto.getCommentaireAdmin());
        demandeRepository.save(demande);

        Utilisateur user = demande.getUtilisateur();
        sendRejectionEmailAsync(user.getEmail(), user.getPrenom(),
                demande.getTypeCompte().name(), dto.getCommentaireAdmin());

        log.info("Account request REJECTED: demandeId={}", demandeId);
        return mapToDTO(demande);
    }

    @Async
    protected void sendConfirmationEmailAsync(String email, String prenom, String typeCompte) {
        try { emailService.sendDemandeConfirmationEmail(email, prenom, typeCompte); }
        catch (Exception e) { log.error("Failed to send confirmation email to {}: {}", email, e.getMessage()); }
    }

    @Async
    protected void sendApprovalEmailAsync(String email, String prenom, String typeCompte, Long compteId) {
        try { emailService.sendDemandeApprovalEmail(email, prenom, typeCompte, compteId); }
        catch (Exception e) { log.error("Failed to send approval email to {}: {}", email, e.getMessage()); }
    }

    @Async
    protected void sendRejectionEmailAsync(String email, String prenom, String typeCompte, String reason) {
        try { emailService.sendDemandeRejectionEmail(email, prenom, typeCompte, reason); }
        catch (Exception e) { log.error("Failed to send rejection email to {}: {}", email, e.getMessage()); }
    }

    private DemandeCompteResponseDTO mapToDTO(DemandeCompte d) {
        DemandeCompteResponseDTO dto = new DemandeCompteResponseDTO();
        dto.setIdDemande(d.getIdDemande());
        dto.setTypeCompte(d.getTypeCompte());
        dto.setMotif(d.getMotif());
        dto.setStatutDemande(d.getStatutDemande());
        dto.setDateDemande(d.getDateDemande());
        dto.setDateDecision(d.getDateDecision());
        dto.setCommentaireAdmin(d.getCommentaireAdmin());
        if (d.getCompteOuvert() != null) dto.setCompteOuvertId(d.getCompteOuvert().getIdCompte());

        Utilisateur u = d.getUtilisateur();
        if (u != null) {
            dto.setUtilisateurId(u.getIdUtilisateur());
            dto.setUtilisateurUsername(u.getUsername());
            dto.setUtilisateurNom(u.getNom());
            dto.setUtilisateurPrenom(u.getPrenom());
            dto.setUtilisateurEmail(u.getEmail());
            dto.setUtilisateurTelephone(u.getTelephone());
            dto.setUtilisateurCin(u.getCin());
        }
        return dto;
    }
}