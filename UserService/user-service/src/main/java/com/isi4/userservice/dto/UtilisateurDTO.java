package com.isi4.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private String idUser;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String mdp;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nomUser;
    
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenomUser;
    
    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;
    
    @NotBlank(message = "Le rôle est obligatoire")
    private String role;
}
