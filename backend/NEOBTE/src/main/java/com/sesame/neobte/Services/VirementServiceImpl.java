package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Virement.VirementCreateDTO;
import com.sesame.neobte.DTO.Responses.Virement.RecipientPreviewDTO;
import com.sesame.neobte.DTO.Responses.Virement.VirementResponseDTO;
import com.sesame.neobte.Entities.Class.*;
import com.sesame.neobte.Entities.Enumeration.StatutCompte;
import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VirementServiceImpl implements VirementService {

    private final IVirementRepository virementRepository;
    private final ICompteRepository compteRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final ICompteInterneRepository compteInterneRepository;
    private final IFraisTransactionRepository fraisTransactionRepository;

    @Value("${neobte.transfer.fee-rate:0.005}")
    private double feeRate;

    @Value("${neobte.fee-account.name:NEOBTE_FEES}")
    private String feeAccountName;

    public VirementServiceImpl(
            IVirementRepository virementRepository,
            ICompteRepository compteRepository,
            IUtilisateurRepository utilisateurRepository,
            ICompteInterneRepository compteInterneRepository,
            IFraisTransactionRepository fraisTransactionRepository) {
        this.virementRepository = virementRepository;
        this.compteRepository = compteRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.compteInterneRepository = compteInterneRepository;
        this.fraisTransactionRepository = fraisTransactionRepository;
    }


    @Override
    public RecipientPreviewDTO resolveRecipient(String identifier) {
        Utilisateur recipient = findByIdentifier(identifier);
        if (recipient == null) return new RecipientPreviewDTO(null, null, null, null, false, feeRate, null);

        Compte primary = getPrimaryAccount(recipient.getIdUtilisateur());
        if (primary == null) return new RecipientPreviewDTO(null, null, null, null, false, feeRate, null);

        return new RecipientPreviewDTO(
                recipient.getPrenom() + " " + recipient.getNom(),
                maskIdentifier(identifier),
                primary.getIdCompte(),
                primary.getTypeCompte().name(),
                true,
                feeRate,
                null  // estimated fee calculated on frontend from feeRate
        );
    }


    @Override
    @Transactional
    public VirementResponseDTO effectuerVirement(VirementCreateDTO dto, Long senderUserId) {

        // Idempotency check
        Optional<Virement> existing = virementRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) return mapToResponseDTO(existing.get());

        if (dto.getMontant() <= 0) throw new BadRequestException("Amount must be greater than 0");

        utilisateurRepository.findById(senderUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        Compte compteSource = getPrimaryAccount(senderUserId);
        if (compteSource == null)
            throw new BadRequestException("You don't have an active bank account. Please open one first.");

        Utilisateur recipient = findByIdentifier(dto.getRecipientIdentifier());

        if (recipient == null)
            throw new ResourceNotFoundException("No user found with that email or phone number");

        if (recipient.getIdUtilisateur().equals(senderUserId))
            throw new BadRequestException("You cannot transfer money to yourself");

        Compte compteDestination = getPrimaryAccount(recipient.getIdUtilisateur());
        if (compteDestination == null)
            throw new BadRequestException("The recipient does not have an active bank account");

        // Calculate tax
        double frais = Math.round(dto.getMontant() * feeRate * 1000.0) / 1000.0;
        double totalDebite = dto.getMontant() + frais;

        // Lock accounts in consistent order (lower id first) to prevent deadlocks
        Long lowId  = Math.min(compteSource.getIdCompte(), compteDestination.getIdCompte());
        Long highId = Math.max(compteSource.getIdCompte(), compteDestination.getIdCompte());

        Compte lockedLow  = compteRepository.findByIdForUpdate(lowId).orElseThrow();
        Compte lockedHigh = compteRepository.findByIdForUpdate(highId).orElseThrow();

        Compte lockedSource = lockedLow.getIdCompte().equals(compteSource.getIdCompte()) ? lockedLow : lockedHigh;
        Compte lockedDest   = lockedLow.getIdCompte().equals(compteDestination.getIdCompte()) ? lockedLow : lockedHigh;

        if (lockedSource.getStatutCompte() != StatutCompte.ACTIVE)
            throw new BadRequestException("Your account is not active");
        if (lockedSource.getSolde() < totalDebite)
            throw new BadRequestException("Insufficient balance (amount + fee: " + totalDebite + " TND)");

        // Debit sender (amount + fee), credit receiver (amount only)
        lockedSource.setSolde(lockedSource.getSolde() - totalDebite);
        lockedDest.setSolde(lockedDest.getSolde() + dto.getMontant());

        compteRepository.save(lockedSource);
        compteRepository.save(lockedDest);

        // Save virement
        Virement virement = new Virement();
        virement.setCompteDe(lockedSource);
        virement.setCompteA(lockedDest);
        virement.setMontant(dto.getMontant());
        virement.setFrais(frais);
        virement.setTauxFrais(feeRate);
        virement.setDateDeVirement(new Date());
        virement.setIdempotencyKey(dto.getIdempotencyKey());
        Virement saved = virementRepository.save(virement);

        // Credit internal fee account + audit record — all in same transaction
        CompteInterne feeAccount = compteInterneRepository.findByNom(feeAccountName)
                .orElseThrow(() -> new IllegalStateException("Internal fee account not found. Check DataInitializer."));
        feeAccount.setSolde(feeAccount.getSolde() + frais);
        compteInterneRepository.save(feeAccount);

        FraisTransaction fraisTransaction = new FraisTransaction();
        fraisTransaction.setVirement(saved);
        fraisTransaction.setMontantFrais(frais);
        fraisTransaction.setTauxApplique(feeRate);
        fraisTransactionRepository.save(fraisTransaction);

        return mapToResponseDTO(saved);
    }

    @Override
    public List<VirementResponseDTO> getVirementsUtilisateur(Long userId) {
        List<Long> compteIds = compteRepository.findByUtilisateur_IdUtilisateur(userId)
                .stream().map(Compte::getIdCompte).toList();

        return virementRepository.findAll().stream()
                .filter(v -> compteIds.contains(v.getCompteDe().getIdCompte())
                        || compteIds.contains(v.getCompteA().getIdCompte()))
                .sorted(Comparator.comparing(Virement::getDateDeVirement).reversed())
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<VirementResponseDTO> getAllVirements() {
        return virementRepository.findAll().stream().map(this::mapToResponseDTO).toList();
    }

    @Override
    public VirementResponseDTO getVirementById(Long virementId) {
        return mapToResponseDTO(virementRepository.findById(virementId)
                .orElseThrow(() -> new ResourceNotFoundException("Virement introuvable")));
    }

    // ── Helpers ──

    private Compte getPrimaryAccount(Long userId) {
        List<Compte> courants = compteRepository
                .findByUtilisateur_IdUtilisateurAndTypeCompteAndStatutCompteOrderByDateCreationAsc(
                        userId, TypeCompte.COURANT, StatutCompte.ACTIVE);
        if (!courants.isEmpty()) return courants.get(0);
        List<Compte> any = compteRepository
                .findByUtilisateur_IdUtilisateurAndStatutCompteOrderByDateCreationAsc(userId, StatutCompte.ACTIVE);
        return any.isEmpty() ? null : any.get(0);
    }

    private Utilisateur findByIdentifier(String identifier) {
        if (identifier == null) return null;
        String trimmed = identifier.trim();
        Utilisateur byEmail = utilisateurRepository.findByEmail(trimmed);
        return byEmail != null ? byEmail : utilisateurRepository.findByTelephone(trimmed);
    }

    private String maskIdentifier(String identifier) {
        if (identifier == null) return null;
        if (identifier.contains("@")) {
            int at = identifier.indexOf('@');
            String local = identifier.substring(0, at);
            String domain = identifier.substring(at);
            return (local.length() <= 2 ? local : local.substring(0, 2)) + "***" + domain;
        }
        return identifier.length() <= 4 ? "****"
                : "*".repeat(identifier.length() - 4) + identifier.substring(identifier.length() - 4);
    }

    private VirementResponseDTO mapToResponseDTO(Virement v) {
        Utilisateur sender    = v.getCompteDe().getUtilisateur();
        Utilisateur recipient = v.getCompteA().getUtilisateur();
        double frais = v.getFrais() != null ? v.getFrais() : 0.0;
        return new VirementResponseDTO(
                v.getIdVirement(),
                v.getCompteDe().getIdCompte(),
                v.getCompteA().getIdCompte(),
                recipient != null ? recipient.getPrenom() + " " + recipient.getNom() : "—",
                sender    != null ? sender.getPrenom()    + " " + sender.getNom()    : "—",
                v.getMontant(),
                frais,
                v.getMontant() + frais,
                v.getTauxFrais(),
                v.getDateDeVirement(),
                v.getIdempotencyKey()
        );
    }
}

