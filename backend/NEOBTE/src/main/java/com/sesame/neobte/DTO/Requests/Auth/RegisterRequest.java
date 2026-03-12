    package com.sesame.neobte.DTO.Requests.Auth;

    import com.sesame.neobte.Entities.Enumeration.Genre;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.Setter;
    import jakarta.validation.constraints.*;

    import java.time.LocalDate;

    @Getter
    @Setter
    @AllArgsConstructor
    //Avec controle de saisie
    public class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain letters, numbers, dots, hyphens and underscores")
        private String username;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String motDePasse;

        @NotBlank(message= "Nom is required")
        private String nom;

        @NotBlank(message= "Prenom is required")
        private String prenom;

        @NotBlank(message = "CIN is required")
        @Size(min = 8, max = 8, message = "CIN must be exactly 8 digits")
        @Pattern(regexp = "^[0-9]{8}$", message = "CIN must contain exactly 8 digits")
        private String cin;

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[+]?[0-9]{8,15}$", message = "Invalid phone number")
        private String telephone;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateNaissance;

        @NotBlank(message= "Job is required")
        private String job;

        private String adresse;
        private Genre genre;
        private String codePostal;
        private String pays;


    }
