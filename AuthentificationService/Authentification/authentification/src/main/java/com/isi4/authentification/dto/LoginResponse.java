package com.isi4.authentification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private String userId;
    private String email;
    private String nom;
    private String prenom;
    private String role;
    
    public LoginResponse(String token, String userId, String email, String nom, String prenom, String role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }
}
