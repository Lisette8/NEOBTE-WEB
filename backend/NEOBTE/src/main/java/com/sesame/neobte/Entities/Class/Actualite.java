package com.sesame.neobte.Entities.Class;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Actualite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idActualite;

    @Column(nullable = false, length = 200)
    private String titre;

    /** Optional subtitle */
    @Column(length = 300)
    private String sousTitre;

    /** Short excerpt shown in lists */
    @Column(length = 600)
    private String description;

    /** Full post content */
    @Column(length = 8000)
    private String contenu;

    /** Optional category/tag */
    @Column(length = 60)
    private String categorie;

    /** Public URL served by backend, e.g. /uploads/actualites/<file> */
    @Column(length = 500)
    private String imageUrl;

    private LocalDateTime dateCreationActualite;
    private LocalDateTime dateMajActualite;

    @ManyToOne
    @JsonIgnore
    private Utilisateur createur;

    @PrePersist
    protected void onCreate() {
        this.dateCreationActualite = LocalDateTime.now();
        this.dateMajActualite = this.dateCreationActualite;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateMajActualite = LocalDateTime.now();
    }
}
