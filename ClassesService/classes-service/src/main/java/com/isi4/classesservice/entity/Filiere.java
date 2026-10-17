package com.isi4.classesservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "filieres", uniqueConstraints = {
    @UniqueConstraint(name = "uk_filieres_nom_ecole", columnNames = {"nom", "id_ecole"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filiere {

    @Id
    @Column(name = "id_filiere", length = 100)
    private String idFiliere;

    @NotBlank
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "id_ecole", length = 100)
    private String idEcole;

    @OneToMany(mappedBy = "filiere", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Classe> classes = new ArrayList<>();
}
