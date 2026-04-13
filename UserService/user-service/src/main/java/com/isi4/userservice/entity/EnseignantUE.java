package com.isi4.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "enseignant_ues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnseignantUE {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String enseignantId;
    
    @Column(nullable = false)
    private Long ueId;
    
    @Column(nullable = false)
    private String codeUE;
    
    @Column(nullable = false)
    private String nomUE;
    
    // Compétences par type de cours
    @Column(nullable = false)
    private Boolean peutCM = false;
    
    @Column(nullable = false)
    private Boolean peutTD = false;
    
    @Column(nullable = false)
    private Boolean peutTP = false;
    
    // Préférence (1-5, 5 étant le plus préféré)
    private Integer preference = 3;
    
    private String commentaire;
}