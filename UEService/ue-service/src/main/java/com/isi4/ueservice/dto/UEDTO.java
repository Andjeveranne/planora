package com.isi4.ueservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UEDTO {

    private Long id;

    private String idUE;

    @NotBlank(message = "Le code UE est obligatoire")
    private String codeUE;

    @NotBlank(message = "Le nom de l'UE est obligatoire")
    private String nomUE;

    private String description;

    private Integer nombreCredit;

    /** Total heures (conservé pour compatibilité) */
    private Integer nombreHeur;

    /** Heures de cours magistral */
    private Integer heuresCM;

    /** Heures de travaux dirigés */
    private Integer heuresTD;

    /** Heures de travaux pratiques */
    private Integer heuresTP;

    private LocalTime heureDebut;
    private LocalTime heureFin;

    /** Niveau ciblé : ING1, ING2, ING3, L1, L2, M1, M2... */
    private String niveau;

    /** Identifiant de la filière */
    @NotBlank(message = "Le département de l'UE est obligatoire")
    private String idFiliere;

    /** Nom de la filière (dénormalisé pour affichage) */
    private String nomFiliere;

    @NotBlank(message = "L'école de l'UE est obligatoire")
    private String idEcole;

    @NotBlank(message = "La spécialité de l'UE est obligatoire")
    private String idSpecialite;

    @NotBlank(message = "Le nom de la spécialité est obligatoire")
    private String specialite;
}
