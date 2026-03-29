package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Notification.NotificationResponseDTO;
import com.sesame.neobte.Entities.Class.Notification;
import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import com.sesame.neobte.Entities.Enumeration.Role;
import com.sesame.neobte.Exceptions.customExceptions.ResourceNotFoundException;
import com.sesame.neobte.Repositories.INotificationRepository;
import com.sesame.neobte.Repositories.IUtilisateurRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final INotificationRepository notificationRepository;
    private final IUtilisateurRepository utilisateurRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<NotificationResponseDTO> getMyNotifications(Long userId, int page, int size, boolean unreadOnly) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(50, Math.max(1, size)));
        Page<Notification> p = unreadOnly
                ? notificationRepository.findByUtilisateur_IdUtilisateurAndLuFalseOrderByDateCreationDesc(userId, pageable)
                : notificationRepository.findByUtilisateur_IdUtilisateurOrderByDateCreationDesc(userId, pageable);
        return p.map(this::mapToDto);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUtilisateur_IdUtilisateurAndLuFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponseDTO markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        if (!n.getUtilisateur().getIdUtilisateur().equals(userId)) {
            throw new ResourceNotFoundException("Notification introuvable");
        }
        if (!n.isLu()) {
            n.setLu(true);
            notificationRepository.save(n);
        }
        return mapToDto(n);
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        notificationRepository.markAllRead(userId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyUser(Long userId, NotificationType type, String titre, String message, String lien) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
        Notification n = new Notification();
        n.setUtilisateur(user);
        n.setType(type);
        n.setTitre(titre);
        n.setMessage(message);
        n.setLien(lien);
        final Notification saved;
        try {
            saved = notificationRepository.save(n);
        } catch (DataIntegrityViolationException e) {
            // Notifications should never block business flows (investment, transfer, etc.).
            // If the DB has an outdated CHECK constraint on `notification.type` (or any other column),
            // we log and skip the notification rather than rolling back the caller's transaction.
            log.warn("[NOTIF] Persist failed userId={} type={} titre='{}': {}",
                    userId, type, titre, e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
            return;
        }

        publishAfterCommit(userId, mapToDto(saved));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyAllClients(NotificationType type, String titre, String message, String lien) {
        List<Utilisateur> clients = utilisateurRepository.findByRole(Role.CLIENT);
        List<Notification> batch = new ArrayList<>(clients.size());
        for (Utilisateur u : clients) {
            Notification n = new Notification();
            n.setUtilisateur(u);
            n.setType(type);
            n.setTitre(titre);
            n.setMessage(message);
            n.setLien(lien);
            batch.add(n);
        }
        final List<Notification> saved;
        try {
            saved = notificationRepository.saveAll(batch);
        } catch (DataIntegrityViolationException e) {
            log.warn("[NOTIF] Batch persist failed type={} titre='{}': {}",
                    type, titre, e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage());
            return;
        }

        // Send a lightweight broadcast event to each user's topic after commit.
        // (We don't broadcast the whole list; only the created notification.)
        publishManyAfterCommit(saved);
    }

    private void publishManyAfterCommit(List<Notification> saved) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            for (Notification n : saved) {
                publish(n.getUtilisateur().getIdUtilisateur(), mapToDto(n));
            }
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Notification n : saved) {
                    publish(n.getUtilisateur().getIdUtilisateur(), mapToDto(n));
                }
            }
        });
    }

    private void publishAfterCommit(Long userId, NotificationResponseDTO dto) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            publish(userId, dto);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(userId, dto);
            }
        });
    }

    private void publish(Long userId, NotificationResponseDTO dto) {
        // Per-user topic (simple broker)
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, dto);
    }

    private NotificationResponseDTO mapToDto(Notification n) {
        return NotificationResponseDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .titre(n.getTitre())
                .message(n.getMessage())
                .lien(n.getLien())
                .lu(n.isLu())
                .dateCreation(n.getDateCreation())
                .build();
    }
}
