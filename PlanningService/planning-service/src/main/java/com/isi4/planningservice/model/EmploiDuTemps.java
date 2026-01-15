package com.isi4.planningservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(nullable = false)
    private String enseignantId; // ID venant du UserService

    @Column(nullable = false)
    private LocalDate dateJour; // Date du cours

    @Column(nullable = false)
    private String jour; // Nom du jour (Lundi, Mardi, etc.)

    @Column(nullable = false)
    private Integer heureDebut; // Heure de début (8, 9, 10, etc.)

    @Column(nullable = false)
    private Integer heureFin; // Heure de fin

    @Column(nullable = false)
    private String ue; // Unité d'enseignement

    @Column(nullable = false)
    private String classe; // Classe (ISI1, ISI2, etc.)

    @Column(nullable = false)
    private String salle; // Salle de cours

    @Column(nullable = false)
    private String typeCours; // CM, TD, TP

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
