package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisponibiliteDTO {
    private Long id;
    private String enseignantId;
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private String commentaire;
}
