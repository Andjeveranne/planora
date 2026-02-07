package com.isi4.ecoleservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité Ecole conforme au diagramme de classes UML.
 * Attributs : IdEcole (String), NomEcole (String).
 * Une Ecole peut avoir des Filiere, Classe et Salle (gérés dans d'autres services).
 */
@Entity
@Table(name = "ecoles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ecole {

    @Id
    @Column(name = "id_ecole", length = 100)
    private String idEcole;

    @NotBlank(message = "Le nom de l'école est obligatoire")
    @Column(name = "nom_ecole", nullable = false, length = 255)
    private String nomEcole;
}
