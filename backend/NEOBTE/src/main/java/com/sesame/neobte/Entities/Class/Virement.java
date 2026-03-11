package com.sesame.neobte.Entities.Class;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Virement implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVirement;


    //idempotencyKey is basically used in a protection layer to prevent the redundant actions when pressing fast on a button for example....
    @Column(unique = true)
    private String idempotencyKey;

    @ManyToOne
    private Compte compteDe;

    @ManyToOne
    private Compte compteA;

    private Double montant;
    private Date dateDeVirement;


}
