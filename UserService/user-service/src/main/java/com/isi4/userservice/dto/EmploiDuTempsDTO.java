package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour l'emploi du temps
 * Utilisé pour les transferts de données entre les couches
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTempsDTO {
    
    private Long id;
    private String enseignantId;
    private LocalDate dateJour;
    private String jour;
    private Integer heureDebut;
    private Integer heureFin;
    private String ue;
    private String classe;
    private String salle;
    private String typeCours;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructeur sans ID (pour création)
     */
    public EmploiDuTempsDTO(String enseignantId, LocalDate dateJour, String jour, 
                           Integer heureDebut, Integer heureFin, String ue, 
                           String classe, String salle, String typeCours) {
        this.enseignantId = enseignantId;
        this.dateJour = dateJour;
        this.jour = jour;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.ue = ue;
        this.classe = classe;
        this.salle = salle;
        this.typeCours = typeCours;
    }

    /**
     * Calcule la durée du cours en heures
     */
    public int getDureeEnHeures() {
        if (heureDebut != null && heureFin != null) {
            return heureFin - heureDebut;
        }
        return 0;
    }

    /**
     * Retourne une représentation textuelle du cours
     */
    public String getCoursTexte() {
        return String.format("%s - %s (%s) de %dh à %dh en %s", 
                ue, classe, typeCours, heureDebut, heureFin, salle);
    }
}