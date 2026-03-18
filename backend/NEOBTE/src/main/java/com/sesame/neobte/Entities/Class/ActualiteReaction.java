package com.sesame.neobte.Entities.Class;

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
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_actualite_reaction_user_post",
                columnNames = {"actualite_id", "utilisateur_id"}
        )
)
public class ActualiteReaction implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "actualite_id", nullable = false)
    private Actualite actualite;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    /**
     * Reaction type code (no emojis), e.g. "LIKE", "CELEBRATE", "SUPPORT".
     * Historical data may still contain legacy emojis.
     */
    @Column(nullable = false, length = 24)
    private String emoji;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateReaction;

    @PrePersist
    protected void onCreate() {
        this.dateReaction = LocalDateTime.now();
    }
}
