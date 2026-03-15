package com.sesame.neobte.Entities.Class;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class CompteInterne implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nom;

    private Double solde = 0.0;

    @Column(nullable = false, updatable = false)
    private Date dateCreation;

    @PrePersist
    protected void onCreate() { this.dateCreation = new Date(); }
}
