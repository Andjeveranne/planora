package com.isi4.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour l'entité Course
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {

    private Long id;

    @NotBlank(message = "Le code du cours est obligatoire")
    private String codeCourse;

    @NotBlank(message = "Le type de cours est obligatoire")
    private String typeCours;

    @NotNull(message = "Le nombre d'heures est obligatoire")
    @Positive(message = "Le nombre d'heures doit être positif")
    private Integer nombreHeures;

    @NotBlank(message = "Le semestre est obligatoire")
    private String semestre;

    private String description;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Relations (IDs pour éviter la récursion)
    @NotNull(message = "L'UE est obligatoire")
    private Long ueId;

    @NotNull(message = "L'enseignant est obligatoire")
    private String enseignantId;

    @NotNull(message = "La classe est obligatoire")
    private Long classeId;

    // Informations détaillées (pour l'affichage)
    private String ueCode;
    private String ueNom;
    private String enseignantNom;
    private String enseignantPrenom;
    private String classeNom;
    private String classeNiveau;

    // Informations calculées
    private Integer nombreCreneaux;
    private Integer heuresProgrammees;
    private Boolean completementProgramme;

    // Liste des emplois du temps (IDs seulement)
    private List<Long> emploiDuTempsIds;

    /**
     * Constructeur pour créer un cours simple
     */
    public CourseDTO(String codeCourse, String typeCours, Integer nombreHeures, 
                     String semestre, String description, Long ueId, 
                     String enseignantId, Long classeId) {
        this.codeCourse = codeCourse;
        this.typeCours = typeCours;
        this.nombreHeures = nombreHeures;
        this.semestre = semestre;
        this.description = description;
        this.ueId = ueId;
        this.enseignantId = enseignantId;
        this.classeId = classeId;
        this.active = true;
    }

    /**
     * Retourne le nom complet du cours
     */
    public String getNomComplet() {
        if (ueCode != null && ueNom != null) {
            return String.format("%s - %s (%s)", ueCode, ueNom, typeCours);
        }
        return String.format("%s (%s)", codeCourse, typeCours);
    }

    /**
     * Retourne les informations de l'enseignant
     */
    public String getInfoEnseignant() {
        if (enseignantPrenom != null && enseignantNom != null) {
            return String.format("%s %s", enseignantPrenom, enseignantNom);
        }
        return "Enseignant non défini";
    }

    /**
     * Retourne les informations de la classe
     */
    public String getInfoClasse() {
        if (classeNom != null) {
            return classeNiveau != null ? 
                String.format("%s (%s)", classeNom, classeNiveau) : classeNom;
        }
        return "Classe non définie";
    }

    /**
     * Vérifie si le cours est valide
     */
    public boolean isValid() {
        return codeCourse != null && !codeCourse.trim().isEmpty() &&
               typeCours != null && !typeCours.trim().isEmpty() &&
               nombreHeures != null && nombreHeures > 0 &&
               semestre != null && !semestre.trim().isEmpty() &&
               ueId != null && enseignantId != null && classeId != null;
    }

    /**
     * Retourne une représentation textuelle du cours
     */
    public String getCourseTexte() {
        return String.format("%s - %s pour %s", 
                getNomComplet(), getInfoEnseignant(), getInfoClasse());
    }
}