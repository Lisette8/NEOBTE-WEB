package com.sesame.neobte.Controllers.Notification;

import com.sesame.neobte.DTO.Responses.Notification.NotificationResponseDTO;
import com.sesame.neobte.Services.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/client/notifications")
@AllArgsConstructor
@PreAuthorize("hasAnyRole('CLIENT','ADMIN')")
public class ClientNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponseDTO> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            Authentication authentication
    ) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.getMyNotifications(userId, page, size, unreadOnly);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Map.of("count", notificationService.getUnreadCount(userId));
    }

    @PostMapping("/{id}/read")
    public NotificationResponseDTO markRead(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return notificationService.markRead(userId, id);
    }

    @PostMapping("/read-all")
    public Map<String, String> markAllRead(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        notificationService.markAllRead(userId);
        return Map.of("message", "Toutes les notifications ont été marquées comme lues.");
    }
}

