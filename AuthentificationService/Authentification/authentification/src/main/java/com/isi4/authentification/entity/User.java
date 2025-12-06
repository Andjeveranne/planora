package com.isi4.authentification.entity;

import com.isi4.authentification.validation.ValidInstitutionalEmail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @Column(name = "id_user")
    private String id;
    
    @ValidInstitutionalEmail
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "mdp", nullable = false)
    private String password;
    
    @Column(name = "nom_user", nullable = false)
    private String nom;
    
    @Column(name = "prenom_user", nullable = false)
    private String prenom;
    
    @Column(nullable = false)
    private String telephone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum Role {
        ADMIN,
        ENSEIGNANT,
        ETUDIANT
    }
}
