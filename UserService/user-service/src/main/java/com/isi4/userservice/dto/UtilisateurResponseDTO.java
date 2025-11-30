package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponseDTO {
    private String idUser;
    private String email;
    private String nomUser;
    private String prenomUser;
    private String telephone;
    private String role;
    private String specialite;
    private String departement;
    // Pas de mot de passe dans la réponse
}
