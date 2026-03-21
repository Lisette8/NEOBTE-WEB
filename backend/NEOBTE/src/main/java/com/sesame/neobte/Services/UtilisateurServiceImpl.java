package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Requests.Client.ChangePasswordRequest;
import com.sesame.neobte.DTO.Requests.Client.UpdateProfileRequest;
import com.sesame.neobte.DTO.Responses.Client.ClientResponse;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Exceptions.customExceptions.BadRequestException;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@AllArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final IUtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;


    @Override
    public Utilisateur createUtilisateur(Utilisateur utilisateur) {
        utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
        return utilisateurRepository.save(utilisateur);
    }

    /** Internal helper to persist updates without re-encoding password. */
    public Utilisateur saveUtilisateur(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }


    @Override
    public Utilisateur getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }


    @Override
    public Utilisateur updateUtilisateur(Long id, UpdateProfileRequest dto) {
        Utilisateur user = getUtilisateurById(id);

        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setTelephone(dto.getTelephone() != null && !dto.getTelephone().isBlank() ? dto.getTelephone().trim() : null);
        user.setJob(dto.getJob() != null && !dto.getJob().isBlank() ? dto.getJob().trim() : null);
        user.setGenre(dto.getGenre());
        user.setAdresse(dto.getAdresse() != null && !dto.getAdresse().isBlank() ? dto.getAdresse().trim() : null);
        user.setCodePostal(dto.getCodePostal() != null && !dto.getCodePostal().isBlank() ? dto.getCodePostal().trim() : null);
        user.setPays(dto.getPays() != null && !dto.getPays().isBlank() ? dto.getPays().trim() : null);

        return utilisateurRepository.save(user);
    }


    @Override
    public void changePassword(Long id, ChangePasswordRequest request) {
        Utilisateur user = getUtilisateurById(id);

        // Anti-spam: allow changing password at most once per 7 days (settings flow).
        if (user.getDateDernierChangementMotDePasse() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextAllowed = user.getDateDernierChangementMotDePasse().plusDays(7);
            if (now.isBefore(nextAllowed)) {
                String when = nextAllowed.format(DateTimeFormatter.ofPattern("d MMMM yyyy à HH:mm", Locale.FRENCH));
                String wait = formatWait(Duration.between(now, nextAllowed));
                throw new BadRequestException(
                        "Vous avez récemment modifié votre mot de passe. " +
                                "Pour des raisons de sécurité, cette action est limitée à une fois tous les 7 jours. " +
                                "Réessayez dans " + wait + " (le " + when + ")."
                );
            }
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getMotDePasse())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect");
        }

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new BadRequestException("Le nouveau mot de passe doit être différent de l'ancien");
        }

        user.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        user.setDateDernierChangementMotDePasse(LocalDateTime.now());
        utilisateurRepository.save(user);

        notificationService.notifyUser(
                id,
                NotificationType.PASSWORD_CHANGED,
                "Mot de passe modifié",
                "Votre mot de passe a été modifié avec succès. Si vous n'êtes pas à l'origine de cette action, contactez le support immédiatement.",
                "/settings-view"
        );
    }

    private static String formatWait(Duration duration) {
        if (duration.isNegative() || duration.isZero()) return "quelques instants";
        long totalMinutes = Math.max(1, duration.toMinutes());
        long days = totalMinutes / (60 * 24);
        long hours = (totalMinutes % (60 * 24)) / 60;
        long minutes = totalMinutes % 60;

        if (days > 0) {
            if (hours > 0) return days + " jour" + (days > 1 ? "s" : "") + " et " + hours + " h";
            return days + " jour" + (days > 1 ? "s" : "");
        }
        if (hours > 0) {
            if (minutes > 0) return hours + " h " + minutes + " min";
            return hours + " h";
        }
        return minutes + " min";
    }


    @Override
    public void deleteUtilisateur(Long id) {
        Utilisateur user = getUtilisateurById(id);
        utilisateurRepository.delete(user);
    }


    // mapper
    public ClientResponse mapToClientResponse(Utilisateur user) {
        return ClientResponse.builder()
                .id(user.getIdUtilisateur())
                .email(user.getEmail())
                .username(user.getUsername())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .photoUrl(user.getPhotoUrl())
                .cin(user.getCin())
                .telephone(user.getTelephone())
                .adresse(user.getAdresse())
                .codePostal(user.getCodePostal())
                .pays(user.getPays())
                .dateNaissance(user.getDateNaissance())
                .job(user.getJob())
                .genre(user.getGenre())
                .role(user.getRole())
                .dateCreationCompte(user.getDateCreationCompte())
                .pinEnabled(user.isPinEnabled())
                .build();
    }
}
