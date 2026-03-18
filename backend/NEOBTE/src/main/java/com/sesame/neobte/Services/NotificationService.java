package com.sesame.neobte.Services;

import com.sesame.neobte.DTO.Responses.Notification.NotificationResponseDTO;
import com.sesame.neobte.Entities.Enumeration.NotificationType;
import org.springframework.data.domain.Page;

public interface NotificationService {
    Page<NotificationResponseDTO> getMyNotifications(Long userId, int page, int size, boolean unreadOnly);
    long getUnreadCount(Long userId);
    NotificationResponseDTO markRead(Long userId, Long notificationId);
    void markAllRead(Long userId);

    void notifyUser(Long userId, NotificationType type, String titre, String message, String lien);
    void notifyAllClients(NotificationType type, String titre, String message, String lien);
}

