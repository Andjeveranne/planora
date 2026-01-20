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
 * DTO pour l'entité Classe
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseDTO {

    private Long id;

    @NotBlank(message = "Le nom de la classe est obligatoire")
    private String nomClasse;

    @NotBlank(message = "Le niveau est obligatoire")
    private String niveau;

    @NotBlank(message = "La filière est obligatoire")
    private String filiere;

    @NotNull(message = "L'année académique est obligatoire")
    private String anneeAcademique;

    @NotNull(message = "Le nombre d'étudiants doit être spécifié")
    @Positive(message = "Le nombre d'étudiants doit être positif")
    private Integer nombreEtudiants;

    private String description;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Informations calculées
    private Integer nombreCours;
    private Integer totalHeuresCours;
    private Integer totalCreditsECTS;

    // Liste des cours (IDs seulement pour éviter la récursion)
    private List<Long> courseIds;

    /**
     * Constructeur pour créer une classe simple
     */
    public ClasseDTO(String nomClasse, String niveau, String filiere, 
                     String anneeAcademique, Integer nombreEtudiants, String description) {
        this.nomClasse = nomClasse;
        this.niveau = niveau;
        this.filiere = filiere;
        this.anneeAcademique = anneeAcademique;
        this.nombreEtudiants = nombreEtudiants;
        this.description = description;
        this.active = true;
    }

    /**
     * Retourne une représentation textuelle de la classe
     */
    public String getClasseTexte() {
        return String.format("%s - %s (%s)", nomClasse, niveau, filiere);
    }

    /**
     * Vérifie si la classe est complète
     */
    public boolean isComplete() {
        return nomClasse != null && !nomClasse.trim().isEmpty() &&
               niveau != null && !niveau.trim().isEmpty() &&
               filiere != null && !filiere.trim().isEmpty() &&
               anneeAcademique != null && !anneeAcademique.trim().isEmpty() &&
               nombreEtudiants != null && nombreEtudiants > 0;
    }
}