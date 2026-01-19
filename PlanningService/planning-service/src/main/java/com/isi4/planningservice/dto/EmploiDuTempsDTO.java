package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
}
