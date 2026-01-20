package com.isi4.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un Course (cours)
 * Lie une UE, un Enseignant et une Classe
 * Basée sur le diagramme de classes fourni
 */
@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le code du cours est obligatoire")
    @Column(name = "code_course", nullable = false, unique = true, length = 50)
    private String codeCourse;

    @NotBlank(message = "Le type de cours est obligatoire")
    @Column(name = "type_cours", nullable = false, length = 20)
    private String typeCours; // CM, TD, TP

    @NotNull(message = "Le nombre d'heures est obligatoire")
    @Positive(message = "Le nombre d'heures doit être positif")
    @Column(name = "nombre_heures", nullable = false)
    private Integer nombreHeures;

    @NotBlank(message = "Le semestre est obligatoire")
    @Column(name = "semestre", nullable = false, length = 10)
    private String semestre; // S1, S2, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relation Many-to-One avec UE
     * Un cours est basé sur une UE
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ue_id", nullable = false)
    private UE ue;

    /**
     * Relation Many-to-One avec Enseignant
     * Un cours est enseigné par un enseignant
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", referencedColumnName = "id_user", nullable = false)
    private Enseignant enseignant;

    /**
     * Relation Many-to-One avec Classe
     * Un cours est dispensé à une classe
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    /**
     * Relation One-to-Many avec EmploiDuTemps
     * Un cours peut avoir plusieurs créneaux dans l'emploi du temps
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmploiDuTemps> emploisDuTemps = new ArrayList<>();

    /**
     * Constructeur pour créer un cours
     */
    public Course(String codeCourse, String typeCours, Integer nombreHeures, String semestre,
                  String description, UE ue, Enseignant enseignant, Classe classe) {
        this.codeCourse = codeCourse;
        this.typeCours = typeCours;
        this.nombreHeures = nombreHeures;
        this.semestre = semestre;
        this.description = description;
        this.ue = ue;
        this.enseignant = enseignant;
        this.classe = classe;
        this.active = true;
    }

    /**
     * Ajoute un emploi du temps à ce cours
     */
    public void addEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.add(emploiDuTemps);
        emploiDuTemps.setCourse(this);
    }

    /**
     * Supprime un emploi du temps de ce cours
     */
    public void removeEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.remove(emploiDuTemps);
        emploiDuTemps.setCourse(null);
    }

    /**
     * Retourne le nombre total d'heures du cours
     */
    public int getTotalHeures() {
        return nombreHeures != null ? nombreHeures : 0;
    }

    /**
     * Retourne le nombre de créneaux programmés
     */
    public int getNombreCreneaux() {
        return emploisDuTemps != null ? emploisDuTemps.size() : 0;
    }

    /**
     * Retourne le nombre d'heures programmées dans l'emploi du temps
     */
    public int getHeuresProgrammees() {
        return emploisDuTemps != null ? emploisDuTemps.stream()
                .mapToInt(edt -> edt.getHeureFin() - edt.getHeureDebut())
                .sum() : 0;
    }

    /**
     * Vérifie si toutes les heures du cours sont programmées
     */
    public boolean isCompletementProgramme() {
        return getHeuresProgrammees() >= getTotalHeures();
    }

    /**
     * Retourne le nom complet du cours
     */
    public String getNomComplet() {
        if (ue != null) {
            return String.format("%s - %s (%s)", ue.getCodeUE(), ue.getNomUE(), typeCours);
        }
        return String.format("%s (%s)", codeCourse, typeCours);
    }

    /**
     * Retourne les informations de la classe
     */
    public String getInfoClasse() {
        return classe != null ? classe.getNomClasse() : "Classe non définie";
    }

    /**
     * Retourne les informations de l'enseignant
     */
    public String getInfoEnseignant() {
        return enseignant != null ? 
            String.format("%s %s", enseignant.getPrenomUser(), enseignant.getNomUser()) : 
            "Enseignant non défini";
    }

    /**
     * Vérifie si le cours est valide
     */
    public boolean isValid() {
        return codeCourse != null && !codeCourse.trim().isEmpty() &&
               typeCours != null && !typeCours.trim().isEmpty() &&
               nombreHeures != null && nombreHeures > 0 &&
               semestre != null && !semestre.trim().isEmpty() &&
               ue != null && enseignant != null && classe != null;
    }

    /**
     * Retourne une représentation textuelle du cours
     */
    public String getCourseTexte() {
        return String.format("%s - %s pour %s", 
                getNomComplet(), getInfoEnseignant(), getInfoClasse());
    }

    /**
     * Met à jour la date de modification
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Initialise les dates de création et modification
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @Override
    public String toString() {
        return String.format("Course{id=%d, code='%s', type='%s', heures=%d}", 
                id, codeCourse, typeCours, nombreHeures);
    }
}