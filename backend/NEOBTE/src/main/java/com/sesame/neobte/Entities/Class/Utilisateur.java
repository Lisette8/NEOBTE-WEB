package com.sesame.neobte.Entities.Class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sesame.neobte.Entities.Converters.BooleanToIntegerConverter;
import com.sesame.neobte.Entities.Enumeration.Genre;
import com.sesame.neobte.Entities.Enumeration.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @JsonIgnore
    private String motDePasse;

    private LocalDateTime dateDernierChangementMotDePasse;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date dateCreationCompte;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true)
    private String cin;

    @Column(unique = true)
    private String telephone;

    private String adresse;
    private String codePostal;
    private String pays;
    private LocalDate dateNaissance;
    private String job;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(length = 500)
    private String photoUrl;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean premium = false;

    @Column(unique = true)
    private String referralCode;

    private LocalDateTime premiumExpiresAt;

    // ── 2FA PIN ──────────────────────────────────────────────────────────────

    /** Whether the user has enabled PIN-based second factor. */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean pinEnabled = false;

    /** Hashed PIN (BCrypt). Null when PIN is not set. */
    @JsonIgnore
    private String pinCode;

    /**
     * Short-lived token issued after password is validated, before PIN is verified.
     * Acts as a "half-authenticated" ticket — not a full JWT.
     */
    @JsonIgnore
    private String pinTempToken;

    private LocalDateTime pinTempTokenExpiry;

    /**
     * When true, this user has defaulted on a loan and cannot request new ones
     * until an admin resets this flag after the situation is resolved.
     */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean loanRestricted = false;

    // ── Relations ─────────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Compte> comptes = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<Support> supports = new ArrayList<>();

    @OneToMany(mappedBy = "utilisateur")
    @JsonIgnore
    private List<DemandeCompte> demandesCompte = new ArrayList<>();
}