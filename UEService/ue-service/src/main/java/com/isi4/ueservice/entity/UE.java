package com.isi4.ueservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité Unité d'enseignement (UE) conforme au diagramme de classes UML.
 * Attributs : IdUE, CodeUE, NomUE, Quantite, Description, NCredit.
 * Une UE regroupe 1..* Cours (gérés dans d'autres services).
 */
@Entity
@Table(name = "ues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UE {

    @Id
    @Column(name = "id_ue", length = 100)
    private String idUE;

    @NotBlank(message = "Le code UE est obligatoire")
    @Column(name = "code_ue", nullable = false, unique = true, length = 50)
    private String codeUE;

    @NotBlank(message = "Le nom de l'UE est obligatoire")
    @Column(name = "nom_ue", nullable = false, length = 255)
    private String nomUE;

    @Column(name = "quantite")
    private Integer quantite;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "n_credit")
    private Integer nCredit;
}
