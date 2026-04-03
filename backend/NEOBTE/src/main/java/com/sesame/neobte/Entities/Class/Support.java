package com.sesame.neobte.Entities.Class;

import com.sesame.neobte.Entities.Enumeration.SupportCategorie;
import com.sesame.neobte.Entities.Enumeration.SupportPriorite;
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
    @Column(length = 2000)
    private String reponseAdmin;

    @Enumerated(EnumType.STRING)
    private SupportStatus status;

    /** Category of the issue — defaults to AUTRE if not provided */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportCategorie categorie = SupportCategorie.AUTRE;

    /** Priority — defaults to NORMALE */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportPriorite priorite = SupportPriorite.NORMALE;

    private LocalDateTime dateCreation;


    @ManyToOne
    private Utilisateur utilisateur;

    /** For unauthenticated contact form submissions — null when sent by a registered user. */
    private String guestEmail;
    private String guestName;
}
