package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmploiDuTempsDTO {
    private Long id;
    private String classeId;
    private String nomClasse;
    private String enseignantId;
    private String nomEnseignant;
    private Long ueId;
    private String codeUE;
    private String nomUE;
    private String salleId;
    private String nomSalle;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String typeCours;
    private Integer semaine;
    private Integer jourSemaine;
    private String creneau;
    
    // Explicit setter for nomEnseignant to ensure compilation
    public void setNomEnseignant(String nomEnseignant) {
        this.nomEnseignant = nomEnseignant;
    }
}