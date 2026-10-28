package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EtudiantDTO extends UtilisateurDTO {
    private String idEcole;
    private String departement;
    private String specialite;
    private String niveau;
    private String classe;
    private String status;
}
