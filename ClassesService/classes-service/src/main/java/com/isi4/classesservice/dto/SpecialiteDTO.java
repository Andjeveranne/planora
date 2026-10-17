package com.isi4.classesservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SpecialiteDTO {
    private String idSpecialite;

    @NotBlank(message = "Le nom de la spécialité est obligatoire")
    private String nom;

    private String description;

    @NotBlank(message = "Le département est obligatoire")
    private String idFiliere;

    private String nomDepartement;
    private String idEcole;
}
