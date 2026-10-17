package com.isi4.salleservice.entity;

import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "salles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Salle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_salle")
    private String idSalle;
    
    @NotBlank(message = "Le nom de la salle est obligatoire")
    @Column(name = "nom_salle", nullable = false)
    private String nomSalle;
    
    @NotNull(message = "Le type de salle est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TypeSalle type;
    
    @NotBlank(message = "La localisation est obligatoire")
    @Column(name = "localisation", nullable = false)
    private String localisation;
    
    @Min(value = 1, message = "La capacité doit être au moins de 1")
    @Column(name = "capacite", nullable = false)
    private Integer capacite;
    
    @Min(value = 1, message = "La capacité d'examen doit être au moins de 1")
    @Column(name = "capacite_exam", nullable = false)
    private Integer capaciteExam;
    
    @NotBlank(message = "L'ID de l'école est obligatoire")
    @Column(name = "id_ecole", nullable = false)
    private String idEcole;

    @Column(name = "id_classe", length = 100)
    private String idClasse;
    
    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutSalle statut;
}