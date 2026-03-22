package com.sesame.neobte.DTO.Requests.DemandeCompte;

import com.sesame.neobte.Entities.Enumeration.TypeCompte;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DemandeCompteCreateDTO {

    @NotNull(message = "Account type is required")
    private TypeCompte typeCompte;

    @Size(max = 500)
    private String motif;

    /**
     * Optional — only required when the user has no CIN on their profile yet (first account).
     * For subsequent accounts the backend reads CIN directly from the user row.
     */
    @Pattern(regexp = "^[0-9]{8}$", message = "CIN must be exactly 8 digits")
    private String cin;

    /**
     * Optional — same logic as CIN above.
     */
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateNaissance;

    // Required for COURANT and PROFESSIONNEL (if not already on profile)
    private String adresse;
    private String job;

    // Required for PROFESSIONNEL only
    private String nomEntreprise;
}