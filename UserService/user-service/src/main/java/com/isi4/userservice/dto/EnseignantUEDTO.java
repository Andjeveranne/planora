package com.isi4.userservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantUEDTO {
    private Long id;
    private String enseignantId;
    private Long ueId;
    private String idEcole;
    private String codeUE;
    private String nomUE;
    private Boolean peutCM;
    private Boolean peutTD;
    private Boolean peutTP;
    private Integer preference;
    private String commentaire;
}