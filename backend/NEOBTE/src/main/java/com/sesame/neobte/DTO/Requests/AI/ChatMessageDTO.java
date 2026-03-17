package com.sesame.neobte.DTO.Requests.AI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    /** "user" or "assistant" */
    private String role;
    private String content;
}
