package com.isi4.planningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "emplois_du_temps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTemps {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "classe_id", nullable = false)
    private String classeId;
    
    /** Nullable pour les créneaux TPE (pas d'enseignant assigné) */
    @Column(name = "enseignant_id")
    private String enseignantId;

    /** Nullable pour les créneaux TPE */
    @Column(name = "ue_id")
    private Long ueId;

    /** Nullable pour les créneaux TPE (pas de salle assignée) */
    @Column(name = "salle_id")
    private String salleId;
    
    @Column(name = "date_heure_debut", nullable = false)
    private LocalDateTime dateHeureDebut;
    
    @Column(name = "date_heure_fin", nullable = false)
    private LocalDateTime dateHeureFin;
    
    @Column(name = "type_cours")
    private String typeCours; // CM, TD, TP
    
    @Column(name = "semaine")
    private Integer semaine;
    
    @Column(name = "jour_semaine")
    private Integer jourSemaine; // 1=Lundi, 2=Mardi, etc.
    
    @Column(name = "creneau")
    private String creneau; // 8h-10h, 10h-12h, etc.
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}