package com.sesame.neobte.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Compte implements Serializable {
    @Id
    private Long idCompte;
    private Double solde;
    @Enumerated(EnumType.STRING)
    private TypeCompte typeCompte;
    @Enumerated(EnumType.STRING)
    private StatutCompte statutCompte;


}
