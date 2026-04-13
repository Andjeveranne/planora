package com.isi4.planningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ue_enseignants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEEnseignant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ue_id", nullable = false)
    private Long ueId;
    
    @Column(name = "enseignant_id", nullable = false)
    private String enseignantId;
    
    @Column(name = "peut_cm")
    private Boolean peutCM = false;
    
    @Column(name = "peut_td")
    private Boolean peutTD = false;
    
    @Column(name = "peut_tp")
    private Boolean peutTP = false;
}