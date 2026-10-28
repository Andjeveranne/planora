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
 * Entité représentant une Unité d'Enseignement (UE)
 * Basée sur le diagramme de classes fourni
 */
@Entity
@Table(name = "ues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le code UE est obligatoire")
    @Column(name = "code_ue", nullable = false, unique = true, length = 50)
    private String codeUE;

    @NotBlank(message = "Le nom de l'UE est obligatoire")
    @Column(name = "nom_ue", nullable = false, length = 255)
    private String nomUE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le nombre d'heures CM est obligatoire")
    @Positive(message = "Le nombre d'heures CM doit être positif")
    @Column(name = "heures_cm", nullable = false)
    private Integer heuresCM;

    @NotNull(message = "Le nombre d'heures TD est obligatoire")
    @Positive(message = "Le nombre d'heures TD doit être positif")
    @Column(name = "heures_td", nullable = false)
    private Integer heuresTD;

    @NotNull(message = "Le nombre d'heures TP est obligatoire")
    @Positive(message = "Le nombre d'heures TP doit être positif")
    @Column(name = "heures_tp", nullable = false)
    private Integer heuresTP;

    @NotNull(message = "Le nombre de crédits ECTS est obligatoire")
    @Positive(message = "Le nombre de crédits ECTS doit être positif")
    @Column(name = "credits_ects", nullable = false)
    private Integer creditsECTS;

    @NotBlank(message = "Le semestre est obligatoire")
    @Column(name = "semestre", nullable = false, length = 10)
    private String semestre; // S1, S2, S3, etc.

    @NotBlank(message = "Le niveau est obligatoire")
    @Column(name = "niveau", nullable = false, length = 50)
    private String niveau; // L1, L2, L3, M1, M2

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Relation Many-to-Many avec Enseignant
     * Une UE peut être enseignée par plusieurs enseignants
     * Un enseignant peut enseigner plusieurs UEs
     */
    @ManyToMany(mappedBy = "ues", fetch = FetchType.LAZY)
    private List<Enseignant> enseignants = new ArrayList<>();

    /**
     * Relation One-to-Many avec EmploiDuTemps
     * Une UE peut avoir plusieurs créneaux dans l'emploi du temps
     */
    @OneToMany(mappedBy = "ue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmploiDuTemps> emploisDuTemps = new ArrayList<>();

    /**
     * Constructeur pour créer une UE
     */
    public UE(String codeUE, String nomUE, String description, Integer heuresCM, 
              Integer heuresTD, Integer heuresTP, Integer creditsECTS, 
              String semestre, String niveau) {
        this.codeUE = codeUE;
        this.nomUE = nomUE;
        this.description = description;
        this.heuresCM = heuresCM;
        this.heuresTD = heuresTD;
        this.heuresTP = heuresTP;
        this.creditsECTS = creditsECTS;
        this.semestre = semestre;
        this.niveau = niveau;
        this.active = true;
    }

    /**
     * Calcule le nombre total d'heures de l'UE
     */
    public Integer getTotalHeures() {
        return heuresCM + heuresTD + heuresTP;
    }

    /**
     * Ajoute un enseignant à cette UE
     */
    public void addEnseignant(Enseignant enseignant) {
        if (!enseignants.contains(enseignant)) {
            enseignants.add(enseignant);
            enseignant.getUes().add(this);
        }
    }

    /**
     * Supprime un enseignant de cette UE
     */
    public void removeEnseignant(Enseignant enseignant) {
        enseignants.remove(enseignant);
        enseignant.getUes().remove(this);
    }

    /**
     * Ajoute un emploi du temps à cette UE
     */
    public void addEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.add(emploiDuTemps);
        emploiDuTemps.setUeEntity(this);
    }

    /**
     * Supprime un emploi du temps de cette UE
     */
    public void removeEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.remove(emploiDuTemps);
        emploiDuTemps.setUeEntity(null);
    }

    /**
     * Vérifie si l'UE est complète (a tous les champs requis)
     */
    public boolean isComplete() {
        return codeUE != null && !codeUE.trim().isEmpty() &&
               nomUE != null && !nomUE.trim().isEmpty() &&
               heuresCM != null && heuresCM > 0 &&
               heuresTD != null && heuresTD >= 0 &&
               heuresTP != null && heuresTP >= 0 &&
               creditsECTS != null && creditsECTS > 0 &&
               semestre != null && !semestre.trim().isEmpty() &&
               niveau != null && !niveau.trim().isEmpty();
    }

    /**
     * Retourne une représentation textuelle de l'UE
     */
    public String getUETexte() {
        return String.format("%s - %s (%d ECTS)", codeUE, nomUE, creditsECTS);
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
        return String.format("UE{id=%d, code='%s', nom='%s', credits=%d}", 
                id, codeUE, nomUE, creditsECTS);
    }
}