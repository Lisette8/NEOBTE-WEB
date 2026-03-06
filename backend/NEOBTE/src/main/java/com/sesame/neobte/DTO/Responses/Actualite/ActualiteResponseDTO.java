package com.sesame.neobte.DTO.Responses.Actualite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ActualiteResponseDTO {

    private Long idActualite;
    private String titre;
    private String description;
    private LocalDateTime dateCreationActualite;
    private Long createurId;

}
