package com.isi4.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "enseignants")
@Data
@EqualsAndHashCode(callSuper = true)
public class Enseignant extends Utilisateur {
    
    @Column(name = "specialite")
    private String specialite;
    
    @Column(name = "departement")
    private String departement;
    
    // Les UE et emploi du temps seront gérés par d'autres services
}
