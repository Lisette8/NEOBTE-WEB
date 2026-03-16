package com.sesame.neobte.Entities.Class.Fraude;

import com.sesame.neobte.Entities.Class.Utilisateur;
import com.sesame.neobte.Entities.Class.Virement;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeAlertType;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeSeverity;
import com.sesame.neobte.Entities.Enumeration.Fraude.FraudeStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FraudeAlerte implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudeAlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudeSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudeStatut statut = FraudeStatut.OPEN;

    /** Human-readable description of why this alert was raised */
    @Column(nullable = false, length = 1000)
    private String description;

    /** Optional admin note when reviewing/dismissing */
    @Column(length = 500)
    private String adminNote;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Utilisateur utilisateur;

    /** The transfer that triggered the alert (nullable for aggregate rules) */
    @ManyToOne
    private Virement virement;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateAlerte;

    private LocalDateTime dateRevue;

    @PrePersist
    protected void onCreate() { this.dateAlerte = LocalDateTime.now(); }
}