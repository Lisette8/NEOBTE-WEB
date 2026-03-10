package com.sesame.neobte.Entities;

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


    private String titre;
    private String description;
    private LocalDateTime dateCreationActualite;

    @ManyToOne
    @JsonIgnore
    private Utilisateur createur;
}
