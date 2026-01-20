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
 * Entité représentant une Classe (groupe d'étudiants)
 * Basée sur le diagramme de classes fourni
 */
@Entity
@Table(name = "classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Classe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom de la classe est obligatoire")
    @Column(name = "nom_classe", nullable = false, unique = true, length = 100)
    private String nomClasse;

    @NotBlank(message = "Le niveau est obligatoire")
    @Column(name = "niveau", nullable = false, length = 50)
    private String niveau; // L1, L2, L3, M1, M2

    @NotBlank(message = "La filière est obligatoire")
    @Column(name = "filiere", nullable = false, length = 100)
    private String filiere; // Informatique, Mathématiques, etc.

    @NotNull(message = "L'année académique est obligatoire")
    @Column(name = "annee_academique", nullable = false, length = 20)
    private String anneeAcademique; // 2023-2024

    @NotNull(message = "Le nombre d'étudiants doit être spécifié")
    @Positive(message = "Le nombre d'étudiants doit être positif")
    @Column(name = "nombre_etudiants", nullable = false)
    private Integer nombreEtudiants;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relation One-to-Many avec Course
     * Une classe peut avoir plusieurs cours
     */
    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    /**
     * Constructeur pour créer une classe
     */
    public Classe(String nomClasse, String niveau, String filiere, String anneeAcademique, 
                  Integer nombreEtudiants, String description) {
        this.nomClasse = nomClasse;
        this.niveau = niveau;
        this.filiere = filiere;
        this.anneeAcademique = anneeAcademique;
        this.nombreEtudiants = nombreEtudiants;
        this.description = description;
        this.active = true;
    }

    /**
     * Ajoute un cours à cette classe
     */
    public void addCourse(Course course) {
        courses.add(course);
        course.setClasse(this);
    }

    /**
     * Supprime un cours de cette classe
     */
    public void removeCourse(Course course) {
        courses.remove(course);
        course.setClasse(null);
    }

    /**
     * Retourne le nombre total de cours pour cette classe
     */
    public int getNombreCours() {
        return courses != null ? courses.size() : 0;
    }

    /**
     * Retourne le nombre total d'heures de cours pour cette classe
     */
    public int getTotalHeuresCours() {
        return courses != null ? courses.stream()
                .mapToInt(Course::getTotalHeures)
                .sum() : 0;
    }

    /**
     * Retourne le nombre total de crédits ECTS pour cette classe
     */
    public int getTotalCreditsECTS() {
        return courses != null ? courses.stream()
                .filter(course -> course.getUe() != null)
                .mapToInt(course -> course.getUe().getCreditsECTS())
                .sum() : 0;
    }

    /**
     * Vérifie si la classe est complète (a tous les champs requis)
     */
    public boolean isComplete() {
        return nomClasse != null && !nomClasse.trim().isEmpty() &&
               niveau != null && !niveau.trim().isEmpty() &&
               filiere != null && !filiere.trim().isEmpty() &&
               anneeAcademique != null && !anneeAcademique.trim().isEmpty() &&
               nombreEtudiants != null && nombreEtudiants > 0;
    }

    /**
     * Retourne une représentation textuelle de la classe
     */
    public String getClasseTexte() {
        return String.format("%s - %s (%s)", nomClasse, niveau, filiere);
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
        return String.format("Classe{id=%d, nom='%s', niveau='%s', filiere='%s'}", 
                id, nomClasse, niveau, filiere);
    }
}