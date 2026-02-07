package com.isi4.ecoleservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EcoleDTO {

    private String idEcole;

    @NotBlank(message = "Le nom de l'école est obligatoire")
    private String nomEcole;
}
