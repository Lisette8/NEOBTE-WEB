package com.sesame.neobte.DTO.Requests.AI;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class ChatRequestDTO {
    @NotBlank
    private String message;

    /** Previous turns in the conversation (may be empty for first message) */
    private List<ChatMessageDTO> history = new ArrayList<>();
}
