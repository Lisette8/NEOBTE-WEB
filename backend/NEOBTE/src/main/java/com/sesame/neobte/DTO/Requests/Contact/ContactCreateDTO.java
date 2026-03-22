package com.sesame.neobte.DTO.Requests.Contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactCreateDTO {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Subject is required")
    @Size(max = 200)
    private String sujet;

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String message;
}
