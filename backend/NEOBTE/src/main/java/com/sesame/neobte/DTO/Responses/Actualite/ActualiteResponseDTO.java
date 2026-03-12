package com.sesame.neobte.DTO.Responses.Actualite;

import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActualiteResponseDTO {

    private Long idActualite;
    private String titre;
    private String description;
    private LocalDateTime dateCreationActualite;
    private Long createurId;

}
