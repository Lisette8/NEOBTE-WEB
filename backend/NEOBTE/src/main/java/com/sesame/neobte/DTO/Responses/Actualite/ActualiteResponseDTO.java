package com.sesame.neobte.DTO.Responses.Actualite;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActualiteResponseDTO {

    private Long idActualite;
    private String titre;
    private String sousTitre;
    private String description;
    private String contenu;
    private String categorie;
    private String imageUrl;
    private LocalDateTime dateCreationActualite;
    private LocalDateTime dateMajActualite;
    private Long createurId;

    /** ReactionType -> count (e.g. LIKE, SUPPORT, CELEBRATE) */
    private Map<String, Long> reactions;
    /** The caller's reaction type (nullable) */
    private String myReaction;

}
