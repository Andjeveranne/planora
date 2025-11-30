package com.isi4.userservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateurs")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    
    @Id
    @Column(name = "id_user")
    private String idUser;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Column(nullable = false)
    private String mdp;
    
    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Column(name = "nom_user", nullable = false)
    private String nomUser;
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Column(name = "prenom_user", nullable = false)
    private String prenomUser;
    
    @Column(nullable = false)
    private String telephone;
    
    @Column(nullable = false)
    private String role; // "ADMIN" ou "ENSEIGNANT"
}
