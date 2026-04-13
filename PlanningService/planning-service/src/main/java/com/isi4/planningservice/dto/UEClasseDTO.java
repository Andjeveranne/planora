package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEClasseDTO {
    private Long id;
    private Long ueId;
    private String codeUE;
    private String nomUE;
    private String classeId;
    private String nomClasse;
    private Integer heuresCM;
    private Integer heuresTD;
    private Integer heuresTP;
}