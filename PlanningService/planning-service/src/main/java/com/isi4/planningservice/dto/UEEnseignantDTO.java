package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEEnseignantDTO {
    private Long id;
    private Long ueId;
    private String codeUE;
    private String nomUE;
    private String enseignantId;
    private Boolean peutCM;
    private Boolean peutTD;
    private Boolean peutTP;
}