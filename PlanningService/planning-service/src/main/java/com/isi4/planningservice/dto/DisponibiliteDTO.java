package com.isi4.planningservice.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DisponibiliteDTO {
    private String enseignantId;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String commentaire;
}
