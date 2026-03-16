package com.sesame.neobte.DTO.Requests.Fraude;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FraudeReviewDTO {

    @NotBlank
    private String newStatut;
    private String adminNote;
}