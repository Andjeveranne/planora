package com.isi4.planningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ue_classes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEClasse {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ue_id", nullable = false)
    private Long ueId;
    
    @Column(name = "classe_id", nullable = false)
    private String classeId;
    
    @Column(name = "heures_cm")
    private Integer heuresCM = 0;
    
    @Column(name = "heures_td")
    private Integer heuresTD = 0;
    
    @Column(name = "heures_tp")
    private Integer heuresTP = 0;
}