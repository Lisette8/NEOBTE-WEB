package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Compte.CompteCreateDTO;
import com.sesame.neobte.DTO.Requests.Compte.UpdateStatutCompteDTO;
import com.sesame.neobte.DTO.Requests.DemandeClotureCompte.DemandeClotureCreateDTO;
import com.sesame.neobte.DTO.Responses.Compte.CompteResponseDTO;
import com.sesame.neobte.DTO.Responses.DemandeClotureCompte.DemandeClotureResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.DemandeClotureCompte;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.StatutDemande;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.IDemandeClotureCompteRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CompteServiceImpl implements CompteService {

    private final ICompteRepository compteRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final IDemandeClotureCompteRepository demandeClotureRepository;

    // ── Basic CRUD ─────────────────────────────────────────────────────────

    @Override
    public CompteResponseDTO createCompte(CompteCreateDTO dto) {
        Utilisateur user = utilisateurRepository.findById(dto.getUtilisateurId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Compte compte = new Compte();
        compte.setSolde(dto.getSolde());
        compte.setTypeCompte(dto.getTypeCompte());
        compte.setStatutCompte(StatutCompte.ACTIVE);
        compte.setUtilisateur(user);
        return mapToDTO(compteRepository.save(compte));
    }

    @Override
    public List<CompteResponseDTO> getAllComptes() {
        return compteRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Override
    public CompteResponseDTO getCompteById(Long id) {
        return mapToDTO(compteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found")));
    }

    @Override
    public void deleteCompteById(Long id) {
        compteRepository.deleteById(id);
    }

    @Override
    public List<CompteResponseDTO> getComptesByUtilisateur(Long userId) {
        return compteRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream().map(this::mapToDTO).toList();
    }

    // ── Client self-service ─────────────────────────────────────────────────

    @Override
    @Transactional
    public CompteResponseDTO suspendreCompte(Long compteId, Long userId) {
        Compte compte = findAndVerifyOwnership(compteId, userId);
        if (compte.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Seul un compte actif peut être suspendu.");
        compte.setStatutCompte(StatutCompte.SUSPENDU);
        log.info("Compte {} suspendu par l'utilisateur {}", compteId, userId);
        return mapToDTO(compteRepository.save(compte));
    }

    @Override
    @Transactional
    public CompteResponseDTO reactiverCompte(Long compteId, Long userId) {
        Compte compte = findAndVerifyOwnership(compteId, userId);
        if (compte.getStatutCompte() != StatutCompte.SUSPENDU)
            throw new BadRequestException("Seul un compte suspendu peut être réactivé.");
        compte.setStatutCompte(StatutCompte.ACTIVE);
        log.info("Compte {} réactivé par l'utilisateur {}", compteId, userId);
        return mapToDTO(compteRepository.save(compte));
    }

    @Override
    @Transactional
    public DemandeClotureResponseDTO demanderCloture(DemandeClotureCreateDTO dto, Long userId) {
        Compte compte = findAndVerifyOwnership(dto.getCompteId(), userId);

        if (compte.getStatutCompte() == StatutCompte.CLOTURE)
            throw new BadRequestException("Ce compte est déjà clôturé.");

        boolean pendingExists = demandeClotureRepository
                .existsByCompte_IdCompteAndStatut(dto.getCompteId(), StatutDemande.EN_ATTENTE);
        if (pendingExists)
            throw new BadRequestException("Une demande de clôture est déjà en cours pour ce compte.");

        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DemandeClotureCompte demande = new DemandeClotureCompte();
        demande.setCompte(compte);
        demande.setUtilisateur(user);
        demande.setMotif(dto.getMotif());
        demande.setStatut(StatutDemande.EN_ATTENTE);

        compte.setStatutCompte(StatutCompte.SUSPENDU);
        compte.setDateSuppressionPrevue(LocalDateTime.now().plusHours(48));
        compteRepository.save(compte);

        log.info("Demande de clôture soumise pour le compte {} — suppression prévue dans 48h", dto.getCompteId());
        return mapClotureToDTO(demandeClotureRepository.save(demande));
    }

    @Override
    public List<DemandeClotureResponseDTO> getMesDemandescloture(Long userId) {
        return demandeClotureRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream().map(this::mapClotureToDTO).toList();
    }

    @Override
    @Transactional
    public CompteResponseDTO annulerCloture(Long compteId, Long userId) {
        Compte compte = findAndVerifyOwnership(compteId, userId);

        boolean pendingExists = demandeClotureRepository
                .existsByCompte_IdCompteAndStatut(compteId, StatutDemande.EN_ATTENTE);
        if (!pendingExists)
            throw new BadRequestException("Aucune demande de clôture en attente pour ce compte.");

        demandeClotureRepository.findByUtilisateur_IdUtilisateur(userId).stream()
                .filter(d -> d.getCompte().getIdCompte().equals(compteId)
                        && d.getStatut() == StatutDemande.EN_ATTENTE)
                .forEach(d -> {
                    d.setStatut(StatutDemande.REJETEE);
                    d.setCommentaireAdmin("Annulée par le client.");
                    d.setDateDecision(LocalDateTime.now());
                    demandeClotureRepository.save(d);
                });

        compte.setStatutCompte(StatutCompte.ACTIVE);
        compte.setDateSuppressionPrevue(null);
        log.info("Clôture annulée par le client pour le compte {}", compteId);
        return mapToDTO(compteRepository.save(compte));
    }

    // ── Admin status management ─────────────────────────────────────────────

    @Override
    @Transactional
    public CompteResponseDTO updateStatutCompte(Long compteId, UpdateStatutCompteDTO dto) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found: " + compteId));
        StatutCompte ancien = compte.getStatutCompte();
        compte.setStatutCompte(dto.getNewStatut());
        log.info("Admin: statut du compte {} changé de {} à {}", compteId, ancien, dto.getNewStatut());
        return mapToDTO(compteRepository.save(compte));
    }

    @Override
    public List<DemandeClotureResponseDTO> getAllDemandesCloture() {
        return demandeClotureRepository.findAllByOrderByStatutAscDateDemandeDesc()
                .stream().map(this::mapClotureToDTO).toList();
    }

    @Override
    @Transactional
    public DemandeClotureResponseDTO approuverCloture(Long demandeId, String commentaire) {
        DemandeClotureCompte demande = demandeClotureRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found: " + demandeId));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE)
            throw new BadRequestException("Cette demande a déjà été traitée.");

        Compte compte = demande.getCompte();

        if (compte.getSolde() != null && compte.getSolde() > 0) {
            compte.setStatutCompte(StatutCompte.BLOQUE);
            demande.setCommentaireAdmin(
                    (commentaire != null ? commentaire + " — " : "") +
                            "Clôture impossible : solde non nul (" + compte.getSolde() + " TND). Veuillez vider le compte.");
        } else {
            compte.setStatutCompte(StatutCompte.CLOTURE);
            demande.setCommentaireAdmin(commentaire);
        }

        compteRepository.save(compte);
        demande.setStatut(StatutDemande.APPROUVEE);
        demande.setDateDecision(LocalDateTime.now());

        log.info("Demande de clôture {} approuvée. Nouveau statut compte : {}", demandeId, compte.getStatutCompte());
        return mapClotureToDTO(demandeClotureRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeClotureResponseDTO rejeterCloture(Long demandeId, String commentaire) {
        DemandeClotureCompte demande = demandeClotureRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande not found: " + demandeId));

        if (demande.getStatut() != StatutDemande.EN_ATTENTE)
            throw new BadRequestException("Cette demande a déjà été traitée.");

        if (commentaire == null || commentaire.isBlank())
            throw new BadRequestException("Un motif est requis pour rejeter une demande.");

        demande.setStatut(StatutDemande.REJETEE);
        demande.setCommentaireAdmin(commentaire);
        demande.setDateDecision(LocalDateTime.now());

        // FIX: Reactivate the account — it was suspended when the client submitted the
        // closure request. Rejecting it means the account goes back to ACTIVE.
        Compte compte = demande.getCompte();
        compte.setStatutCompte(StatutCompte.ACTIVE);
        compte.setDateSuppressionPrevue(null);
        compteRepository.save(compte);

        log.info("Demande de clôture {} rejetée — compte {} réactivé.", demandeId, compte.getIdCompte());
        return mapClotureToDTO(demandeClotureRepository.save(demande));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Compte findAndVerifyOwnership(Long compteId, Long userId) {
        Compte compte = compteRepository.findById(compteId)
                .orElseThrow(() -> new ResourceNotFoundException("Compte not found: " + compteId));
        if (!compte.getUtilisateur().getIdUtilisateur().equals(userId))
            throw new BadRequestException("Vous n'êtes pas propriétaire de ce compte.");
        return compte;
    }

    private CompteResponseDTO mapToDTO(Compte compte) {
        CompteResponseDTO dto = new CompteResponseDTO();
        dto.setIdCompte(compte.getIdCompte());
        dto.setSolde(compte.getSolde());
        dto.setTypeCompte(compte.getTypeCompte());
        dto.setStatutCompte(compte.getStatutCompte());
        if (compte.getUtilisateur() != null)
            dto.setUtilisateurId(compte.getUtilisateur().getIdUtilisateur());
        dto.setDateSuppressionPrevue(compte.getDateSuppressionPrevue());
        return dto;
    }

    private DemandeClotureResponseDTO mapClotureToDTO(DemandeClotureCompte d) {
        DemandeClotureResponseDTO dto = new DemandeClotureResponseDTO();
        dto.setId(d.getId());
        dto.setMotif(d.getMotif());
        dto.setStatut(d.getStatut());
        dto.setCommentaireAdmin(d.getCommentaireAdmin());
        dto.setDateDemande(d.getDateDemande());
        dto.setDateDecision(d.getDateDecision());
        if (d.getCompte() != null) {
            dto.setCompteId(d.getCompte().getIdCompte());
            dto.setTypeCompte(d.getCompte().getTypeCompte() != null ? d.getCompte().getTypeCompte().name() : null);
            dto.setSoldeAtDemande(d.getCompte().getSolde());
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