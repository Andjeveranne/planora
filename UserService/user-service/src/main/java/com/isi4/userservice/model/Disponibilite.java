package com.isi4.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Entité représentant la disponibilité d'un enseignant
 * Basée sur le diagramme de classes fourni
 */
@Entity
@Table(name = "disponibilites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Disponibilite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Le jour de la semaine est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "jour_semaine", nullable = false)
    private DayOfWeek jourSemaine;

    @NotNull(message = "L'heure de début est obligatoire")
    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @NotNull(message = "L'heure de fin est obligatoire")
    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    /**
     * Relation Many-to-One avec Enseignant
     * Un enseignant peut avoir plusieurs créneaux de disponibilité
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enseignant_id", nullable = false)
    private Enseignant enseignant;

    /**
     * Constructeur pour créer une disponibilité
     */
    public Disponibilite(DayOfWeek jourSemaine, LocalTime heureDebut, LocalTime heureFin, Enseignant enseignant) {
        this.jourSemaine = jourSemaine;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.enseignant = enseignant;
    }

    /**
     * Vérifie si les heures sont cohérentes
     */
    public boolean isHeuresValides() {
        return heureDebut != null && heureFin != null && heureDebut.isBefore(heureFin);
    }

    /**
     * Vérifie si ce créneau chevauche avec un autre
     */
    public boolean chevaucheAvec(Disponibilite autre) {
        if (autre == null || !this.jourSemaine.equals(autre.jourSemaine)) {
            return false;
        }
        
        return !(this.heureFin.isBefore(autre.heureDebut) || 
                 this.heureDebut.isAfter(autre.heureFin) ||
                 this.heureFin.equals(autre.heureDebut) ||
                 this.heureDebut.equals(autre.heureFin));
    }

    /**
     * Retourne une représentation textuelle du créneau
     */
    public String getCreneauTexte() {
        return String.format("%s de %s à %s", 
                jourSemaine.name(), 
                heureDebut.toString(), 
                heureFin.toString());
    }

    @Override
    public String toString() {
        return String.format("Disponibilite{id=%d, jour=%s, %s-%s}", 
                id, jourSemaine, heureDebut, heureFin);
    }
}