package com.isi4.classesservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiliereDTO {
    private String idFiliere;

    @NotBlank(message = "Le nom de la filière est obligatoire")
    private String nom;

    private String description;

    private String idEcole;

    private List<ClasseDTO> classes;
}
