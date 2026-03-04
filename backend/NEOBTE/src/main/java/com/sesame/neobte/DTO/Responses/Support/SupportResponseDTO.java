package com.sesame.neobte.DTO.Responses.Support;

import lombok.*;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupportResponseDTO {
    private Long idSupport;
    private String sujet;
    private String message;
    private String reponseAdmin;
    private String status;
    private LocalDateTime dateCreation;
}
