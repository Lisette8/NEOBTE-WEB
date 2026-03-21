package com.sesame.neobte.Controllers.Search;


import com.sesame.neobte.DTO.Responses.Search.GlobalSearchResponseDTO;
import com.sesame.neobte.Entities.Class.Compte;
import com.sesame.neobte.Entities.Class.Support;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Class.Virement;
import com.sesame.neobte.Repositories.ICompteRepository;
import com.sesame.neobte.Repositories.ISupportRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import com.sesame.neobte.Repositories.IVirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/search")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminSearchController {

    private final IUtilisateurRepository utilisateurRepository;
    private final ICompteRepository compteRepository;
    private final IVirementRepository virementRepository;
    private final ISupportRepository supportRepository;

    /**
     * Global search — accepts a query string and searches across:
     * - Users: by ID, email, name, CIN, phone
     * - Accounts: by ID, or by owner's ID/email
     * - Transfers: by ID, or by account ID
     * - Support tickets: by ID, or by user email/subject
     */
    @GetMapping
    public GlobalSearchResponseDTO search(@RequestParam String q) {
        String query = q.trim().toLowerCase();

        return GlobalSearchResponseDTO.builder()
                .users(searchUsers(query))
                .accounts(searchAccounts(query))
                .transfers(searchTransfers(query))
                .tickets(searchTickets(query))
                .build();
    }

    // ── Users ────────────────────────────────────────────────────────────────
    private List<GlobalSearchResponseDTO.UserResult> searchUsers(String q) {
        List<Utilisateur> all = utilisateurRepository.findAll();
        List<GlobalSearchResponseDTO.UserResult> results = new ArrayList<>();

        for (Utilisateur u : all) {
            if (matchesUser(u, q)) {
                results.add(GlobalSearchResponseDTO.UserResult.builder()
                        .id(u.getIdUtilisateur())
                        .fullName(u.getPrenom() + " " + u.getNom())
                        .email(u.getEmail())
                        .role(u.getRole().name())
                        .premium(u.isPremium())
                        .build());
            }
        }
        return results;
    }

    private boolean matchesUser(Utilisateur u, String q) {
        if (isNumeric(q) && u.getIdUtilisateur().toString().equals(q)) return true;
        if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true;
        if (u.getNom() != null && u.getNom().toLowerCase().contains(q)) return true;
        if (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(q)) return true;
        if (u.getCin() != null && u.getCin().toLowerCase().contains(q)) return true;
        if (u.getTelephone() != null && u.getTelephone().contains(q)) return true;
        if (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) return true;
        return false;
    }

    // ── Accounts ─────────────────────────────────────────────────────────────
    private List<GlobalSearchResponseDTO.AccountResult> searchAccounts(String q) {
        List<Compte> all = compteRepository.findAll();
        List<GlobalSearchResponseDTO.AccountResult> results = new ArrayList<>();

        for (Compte c : all) {
            if (matchesCompte(c, q)) {
                String ownerName = c.getUtilisateur() != null
                        ? c.getUtilisateur().getPrenom() + " " + c.getUtilisateur().getNom()
                        : "—";
                results.add(GlobalSearchResponseDTO.AccountResult.builder()
                        .id(c.getIdCompte())
                        .type(c.getTypeCompte() != null ? c.getTypeCompte().name() : "—")
                        .statut(c.getStatutCompte() != null ? c.getStatutCompte().name() : "—")
                        .solde(c.getSolde())
                        .userId(c.getUtilisateur() != null ? c.getUtilisateur().getIdUtilisateur() : null)
                        .userFullName(ownerName)
                        .build());
            }
        }
        return results;
    }

    private boolean matchesCompte(Compte c, String q) {
        if (isNumeric(q) && c.getIdCompte().toString().equals(q)) return true;
        if (c.getUtilisateur() != null) {
            Utilisateur u = c.getUtilisateur();
            if (isNumeric(q) && u.getIdUtilisateur().toString().equals(q)) return true;
            if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true;
            if (u.getNom() != null && u.getNom().toLowerCase().contains(q)) return true;
            if (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(q)) return true;
        }
        return false;
    }

    // ── Transfers ────────────────────────────────────────────────────────────
    private List<GlobalSearchResponseDTO.TransferResult> searchTransfers(String q) {
        List<Virement> all = virementRepository.findAll();
        List<GlobalSearchResponseDTO.TransferResult> results = new ArrayList<>();

        for (Virement v : all) {
            if (matchesVirement(v, q)) {
                String sender = v.getCompteDe() != null && v.getCompteDe().getUtilisateur() != null
                        ? v.getCompteDe().getUtilisateur().getPrenom() + " " + v.getCompteDe().getUtilisateur().getNom()
                        : "—";
                String recipient = v.getCompteA() != null && v.getCompteA().getUtilisateur() != null
                        ? v.getCompteA().getUtilisateur().getPrenom() + " " + v.getCompteA().getUtilisateur().getNom()
                        : "—";
                results.add(GlobalSearchResponseDTO.TransferResult.builder()
                        .id(v.getIdVirement())
                        .montant(v.getMontant())
                        .senderName(sender)
                        .recipientName(recipient)
                        .date(v.getDateDeVirement())
                        .build());
            }
        }
        return results;
    }

    private boolean matchesVirement(Virement v, String q) {
        if (isNumeric(q) && v.getIdVirement().toString().equals(q)) return true;
        if (v.getCompteDe() != null) {
            if (isNumeric(q) && v.getCompteDe().getIdCompte().toString().equals(q)) return true;
            Utilisateur sender = v.getCompteDe().getUtilisateur();
            if (sender != null) {
                if (sender.getEmail() != null && sender.getEmail().toLowerCase().contains(q)) return true;
                if ((sender.getPrenom() + " " + sender.getNom()).toLowerCase().contains(q)) return true;
            }
        }
        if (v.getCompteA() != null) {
            if (isNumeric(q) && v.getCompteA().getIdCompte().toString().equals(q)) return true;
            Utilisateur recipient = v.getCompteA().getUtilisateur();
            if (recipient != null) {
                if (recipient.getEmail() != null && recipient.getEmail().toLowerCase().contains(q)) return true;
                if ((recipient.getPrenom() + " " + recipient.getNom()).toLowerCase().contains(q)) return true;
            }
        }
        return false;
    }

    // ── Support tickets ──────────────────────────────────────────────────────
    private List<GlobalSearchResponseDTO.SupportResult> searchTickets(String q) {
        List<Support> all = supportRepository.findAll();
        List<GlobalSearchResponseDTO.SupportResult> results = new ArrayList<>();

        for (Support s : all) {
            if (matchesSupport(s, q)) {
                results.add(GlobalSearchResponseDTO.SupportResult.builder()
                        .id(s.getIdSupport())
                        .sujet(s.getSujet())
                        .status(s.getStatus() != null ? s.getStatus().name() : "—")
                        .userEmail(s.getUtilisateur() != null ? s.getUtilisateur().getEmail() : "—")
                        .build());
            }
        }
        return results;
    }

    private boolean matchesSupport(Support s, String q) {
        if (isNumeric(q) && s.getIdSupport().toString().equals(q)) return true;
        if (s.getSujet() != null && s.getSujet().toLowerCase().contains(q)) return true;
        if (s.getUtilisateur() != null) {
            Utilisateur u = s.getUtilisateur();
            if (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)) return true;
            if ((u.getPrenom() + " " + u.getNom()).toLowerCase().contains(q)) return true;
        }
        return false;
    }

    private boolean isNumeric(String s) {
        if (s == null || s.isEmpty()) return false;
        for (char c : s.toCharArray()) if (!Character.isDigit(c)) return false;
        return true;
    }
}