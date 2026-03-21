    package com.sesame.neobte.DTO.Requests.Auth;


    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import jakarta.validation.constraints.*;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    //Avec controle de saisie
    public class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        private String motDePasse;

        @NotBlank(message = "First name is required")
        private String prenom;

        @NotBlank(message = "Last name is required")
        private String nom;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Invalid phone number")
        private String telephone;
    }
