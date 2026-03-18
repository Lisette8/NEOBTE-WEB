package com.sesame.neobte.DTO.Responses.Notification;

import com.sesame.neobte.Entities.Enumeration.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponseDTO {
    private Long id;
    private NotificationType type;
    private String titre;
    private String message;
    private String lien;
    private boolean lu;
    private LocalDateTime dateCreation;
}

