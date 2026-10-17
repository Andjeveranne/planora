package com.isi4.classesservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "classes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Classe {

    @Id
    @Column(name = "id_classe", length = 100)
    private String idClasse;

    @NotBlank
    @Column(name = "nom_classe", nullable = false, unique = true, length = 100)
    private String nomClasse;

    @NotNull
    @Positive
    @Column(name = "effectifs", nullable = false)
    private Integer effectifs;

    @NotBlank
    @Column(name = "niveau", nullable = false, length = 50)
    private String niveau;

    @Column(name = "specialite", length = 100)
    private String specialite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_filiere", nullable = false)
    private Filiere filiere;
}
