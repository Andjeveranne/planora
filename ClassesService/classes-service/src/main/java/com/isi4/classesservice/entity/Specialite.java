package com.isi4.classesservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "specialites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_specialites_nom_departement", columnNames = {"nom", "id_filiere"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Specialite {

    @Id
    @Column(name = "id_specialite", length = 100)
    private String idSpecialite;

    @NotBlank
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    @NotBlank
    @Column(name = "id_filiere", nullable = false, length = 100)
    private String idFiliere;
}
