package com.sesame.neobte.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private Long idActualite;

    private String genre;
    private Date dateCreationActualite;
    private Long createur;
    private String titre;
    private String description;
}
