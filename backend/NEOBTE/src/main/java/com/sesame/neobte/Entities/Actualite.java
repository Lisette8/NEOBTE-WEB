package com.sesame.neobte.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

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
    private Date dateCreationActualite;

    @ManyToOne
    private Utilisateur createur;
}
