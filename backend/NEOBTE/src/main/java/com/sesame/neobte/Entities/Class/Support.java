package com.sesame.neobte.Entities.Class;

import com.sesame.neobte.Entities.Enumeration.SupportStatus;
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
public class Support implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSupport;

    private String sujet;
    private String message;
    private String reponseAdmin;

    @Enumerated(EnumType.STRING)
    private SupportStatus status;

    private LocalDateTime dateCreation;


    @ManyToOne
    private Utilisateur utilisateur;
}
