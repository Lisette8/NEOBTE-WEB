package com.sesame.neobte.DTO.Responses.Fraude;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FraudeAlerteResponseDTO {
    private Long id;
    private String type;
    private String severity;
    private String statut;
    private String description;
    private String adminNote;

    private Long utilisateurId;
    private String utilisateurNom;
    private String utilisateurEmail;

    private Long virementId;
    private Double virementMontant;

    private LocalDateTime dateAlerte;
    private LocalDateTime dateRevue;
}