package com.isi4.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "etudiants")
@Data
@EqualsAndHashCode(callSuper = true)
public class Etudiant extends Utilisateur {

    @Column(name = "id_ecole", length = 100)
    private String idEcole;

    @Column(name = "departement", length = 100)
    private String departement;

    @Column(name = "specialite", length = 100)
    private String specialite;

    @Column(name = "niveau", length = 50)
    private String niveau;

    @Column(name = "classe", length = 100)
    private String classe;

    @Column(name = "status", length = 50)
    private String status = "Actif";
}
