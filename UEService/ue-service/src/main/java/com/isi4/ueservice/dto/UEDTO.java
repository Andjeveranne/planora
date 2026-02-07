package com.isi4.ueservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEDTO {

    private String idUE;

    @NotBlank(message = "Le code UE est obligatoire")
    private String codeUE;

    @NotBlank(message = "Le nom de l'UE est obligatoire")
    private String nomUE;

    private Integer quantite;

    private String description;

    private Integer nCredit;
}
