package com.isi4.ueservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UE {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_ue", length = 100, unique = true)
    private String idUE;

    @NotBlank(message = "Le code UE est obligatoire")
    @Column(name = "code_ue", nullable = false, unique = true, length = 50)
    private String codeUE;

    @NotBlank(message = "Le nom de l'UE est obligatoire")
    @Column(name = "nom_ue", nullable = false, length = 255)
    private String nomUE;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "nombre_credit")
    private Integer nombreCredit;

    /** Total heures (conservé pour compatibilité) */
    @Column(name = "nombre_heur")
    private Integer nombreHeur;

    /** Heures de cours magistral */
    @Column(name = "heures_cm")
    private Integer heuresCM;

    /** Heures de travaux dirigés */
    @Column(name = "heures_td")
    private Integer heuresTD;

    /** Heures de travaux pratiques */
    @Column(name = "heures_tp")
    private Integer heuresTP;

    /** Niveau ciblé : ING1, ING2, ING3, ING4, ING5, L1, L2, L3, M1, M2... */
    @Column(name = "niveau", length = 50)
    private String niveau;

    /** Identifiant de la filière dans ClassesService */
    @Column(name = "id_filiere", length = 100)
    private String idFiliere;

    /** Nom de la filière (dénormalisé pour affichage) */
    @Column(name = "nom_filiere", length = 100)
    private String nomFiliere;
}
