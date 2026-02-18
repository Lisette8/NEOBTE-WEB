    package com.sesame.neobte.DTO.Requests.Auth;

    import com.sesame.neobte.Entities.Genre;
    import lombok.AllArgsConstructor;
    import lombok.Getter;
    import lombok.Setter;
    import jakarta.validation.constraints.*;

    @Getter
    @Setter
    @AllArgsConstructor
    //Avec controle de saisie
    public class RegisterRequest {

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;


        @NotBlank(message= "Nom is required")
        private String nom;

        @NotBlank(message= "Prenom is required")
        private String prenom;


        private String adresse;

        @NotNull(message = "Age is required")
        @Min(18)
        private Integer age;

        @NotBlank(message= "Job is required")
        private String job;

        private Genre genre;


        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String motDePasse;
    }
