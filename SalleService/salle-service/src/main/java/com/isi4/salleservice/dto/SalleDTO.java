package com.isi4.salleservice.dto;

import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalleDTO {
    
    private String idSalle;
    
    @NotBlank(message = "Le nom de la salle est obligatoire")
    private String nomSalle;
    
    @NotNull(message = "Le type de salle est obligatoire")
    private TypeSalle type;
    
    @NotBlank(message = "La localisation est obligatoire")
    private String localisation;
    
    @Min(value = 1, message = "La capacité doit être au moins de 1")
    private Integer capacite;
    
    @Min(value = 1, message = "La capacité d'examen doit être au moins de 1")
    private Integer capaciteExam;
    
    @NotBlank(message = "L'ID de l'école est obligatoire")
    private String idEcole;
    
    @NotNull(message = "Le statut est obligatoire")
    private StatutSalle statut;
}