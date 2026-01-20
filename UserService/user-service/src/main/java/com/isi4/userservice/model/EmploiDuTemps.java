package com.isi4.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant un emploi du temps d'un enseignant
 * Migrée depuis PlanningService vers UserService
 */
@Entity
@Table(name = "emplois_du_temps")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTemps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La date du jour est obligatoire")
    @Column(name = "date_jour", nullable = false)
    private LocalDate dateJour;

    @NotNull(message = "Le jour est obligatoire")
    @Column(name = "jour", nullable = false)
    private String jour;

    @NotNull(message = "L'heure de début est obligatoire")
    @Column(name = "heure_debut", nullable = false)
    private Integer heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    @Column(name = "heure_fin", nullable = false)
    private Integer heureFin;

    @NotNull(message = "L'UE est obligatoire")
    @Column(name = "ue", nullable = false)
    private String ue;

    @NotNull(message = "La classe est obligatoire")
    @Column(name = "classe", nullable = false)
    private String classe;

    @NotNull(message = "La salle est obligatoire")
    @Column(name = "salle", nullable = false)
    private String salle;

    @NotNull(message = "Le type de cours est obligatoire")
    @Column(name = "type_cours", nullable = false)
    private String typeCours;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relation Many-to-One avec Enseignant
     * Un enseignant peut avoir plusieurs cours dans son emploi du temps
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", referencedColumnName = "id_user", nullable = false)
    private Enseignant enseignant;

    /**
     * Relation Many-to-One avec UE
     * Une UE peut avoir plusieurs créneaux dans l'emploi du temps
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id")
    private UE ueEntity;

    /**
     * Relation Many-to-One avec Course
     * Un cours peut avoir plusieurs créneaux dans l'emploi du temps
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /**
     * Constructeur pour créer un emploi du temps
     */
    public EmploiDuTemps(LocalDate dateJour, String jour, Integer heureDebut, Integer heureFin,
                        String ue, String classe, String salle, String typeCours, Enseignant enseignant) {
        this.dateJour = dateJour;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ue = ue;
        this.classe = classe;
        this.salle = salle;
        this.typeCours = typeCours;
        this.enseignant = enseignant;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifie si les heures sont cohérentes
     */
    public boolean isHeuresValides() {
        return heureDebut != null && heureFin != null && heureDebut < heureFin;
    }

    /**
     * Calcule la durée du cours en heures
     */
    public int getDureeEnHeures() {
        return heureFin - heureDebut;
    }

    /**
     * Vérifie si ce cours chevauche avec un autre
     */
    public boolean chevaucheAvec(EmploiDuTemps autre) {
        if (autre == null || !this.dateJour.equals(autre.dateJour)) {
            return false;
        }
        
        return !(this.heureFin <= autre.heureDebut || this.heureDebut >= autre.heureFin);
    }

    /**
     * Met à jour la date de modification
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Initialise la date de création
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Retourne une représentation textuelle du cours
     */
    public String getCoursTexte() {
        return String.format("%s - %s (%s) de %dh à %dh en %s", 
                ue, classe, typeCours, heureDebut, heureFin, salle);
    }

    @Override
    public String toString() {
        return String.format("EmploiDuTemps{id=%d, date=%s, cours=%s %s-%s, salle=%s}", 
                id, dateJour, ue, heureDebut, heureFin, salle);
    }
}