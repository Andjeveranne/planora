package com.isi4.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO pour la gestion des disponibilités des enseignants
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteDTO {

    private Long id;

    @NotNull(message = "Le jour de la semaine est obligatoire")
    private DayOfWeek jourSemaine;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime heureFin;

    private String enseignantId; // Changé de Long à String

    /**
     * Constructeur pour créer un DTO sans ID (pour la création)
     */
    public DisponibiliteDTO(DayOfWeek jourSemaine, LocalTime heureDebut, LocalTime heureFin, String enseignantId) {
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.enseignantId = enseignantId;
    }

    /**
     * Vérifie si les heures sont cohérentes
     */
    public boolean isHeuresValides() {
        return heureDebut != null && heureFin != null && heureDebut.isBefore(heureFin);
    }

    /**
     * Retourne une représentation textuelle du créneau
     */
    public String getCreneauTexte() {
        return String.format("%s de %s à %s", 
                jourSemaine != null ? jourSemaine.name() : "N/A", 
                heureDebut != null ? heureDebut.toString() : "N/A", 
                heureFin != null ? heureFin.toString() : "N/A");
    }
}