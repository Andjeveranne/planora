package com.isi4.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enseignants")
@Data
@EqualsAndHashCode(callSuper = true)
public class Enseignant extends Utilisateur {
    
    @Column(name = "specialite")
    private String specialite;
    
    @Column(name = "departement")
    private String departement;
    
    /**
     * Relation One-to-Many avec Disponibilite
     * Un enseignant peut avoir plusieurs créneaux de disponibilité
     */
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Disponibilite> disponibilites = new ArrayList<>();
    
    // Les UE et emploi du temps seront gérés par d'autres services
}
