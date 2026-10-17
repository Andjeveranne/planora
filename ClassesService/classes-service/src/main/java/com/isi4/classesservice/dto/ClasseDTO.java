package com.isi4.classesservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClasseDTO {
    private String idClasse;

    @NotBlank(message = "Le nom de la classe est obligatoire")
    private String nomClasse;

    @NotNull(message = "L'effectif est obligatoire")
    @Positive(message = "L'effectif doit être positif")
    private Integer effectifs;

    @NotBlank(message = "Le niveau est obligatoire")
    private String niveau;

    @NotBlank(message = "L'identifiant de la filière est obligatoire")
    private String idFiliere;

    @NotBlank(message = "La spécialité est obligatoire")
    private String specialite;

    private String nomFiliere;

    private String idEcole;
}
