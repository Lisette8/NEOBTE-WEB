package com.sesame.neobte.Entities;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
    private Long idVirement;

    @ManyToOne
    private Compte compteDe;

    @ManyToOne
    private Compte compteA;

    private Double montant;
    private Date dateDeVirement;
}
