package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour les Unités d'Enseignement (UE)
 * Utilisé pour les transferts de données entre les couches
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEDTO {
    
    private Long id;
    private String codeUE;
    private String nomUE;
    private String description;
    private Integer heuresCM;
    private Integer heuresTD;
    private Integer heuresTP;
    private Integer creditsECTS;
    private String semestre;
    private String niveau;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Relations
    private List<String> enseignantIds;
    private List<String> enseignantNames;
    private Integer nombreEnseignants;
    private Integer nombreEmploisDuTemps;

    /**
     * Constructeur pour création (sans ID)
     */
    public UEDTO(String codeUE, String nomUE, String description, Integer heuresCM, 
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
        if (heuresCM != null && heuresTD != null && heuresTP != null) {
            return heuresCM + heuresTD + heuresTP;
        }
        return 0;
    }

    /**
     * Retourne une représentation textuelle de l'UE
     */
    public String getUETexte() {
        return String.format("%s - %s (%d ECTS)", codeUE, nomUE, creditsECTS);
    }

    /**
     * Vérifie si l'UE est complète
     */
    public boolean isComplete() {
        return codeUE != null && !codeUE.trim().isEmpty() &&
               nomUE != null && !nomUE.trim().isEmpty() &&
               heuresCM != null && heuresCM >= 0 &&
               heuresTD != null && heuresTD >= 0 &&
               heuresTP != null && heuresTP >= 0 &&
               creditsECTS != null && creditsECTS > 0 &&
               semestre != null && !semestre.trim().isEmpty() &&
               niveau != null && !niveau.trim().isEmpty();
    }

    /**
     * Retourne le type de cours dominant
     */
    public String getTypeCoursDominant() {
        if (heuresCM == null || heuresTD == null || heuresTP == null) {
            return "Non défini";
        }
        
        if (heuresCM >= heuresTD && heuresCM >= heuresTP) {
            return "CM";
        } else if (heuresTD >= heuresTP) {
            return "TD";
        } else {
            return "TP";
        }
    }
}