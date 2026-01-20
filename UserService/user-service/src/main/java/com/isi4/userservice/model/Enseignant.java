package com.isi4.userservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "enseignants")
@Data
@EqualsAndHashCode(callSuper = true)
public class Enseignant extends Utilisateur {
    
    @Column(name = "specialite")
    private String specialite;
    
    @Column(name = "departement")
    private String departement;
    
    /**
     * Relation One-to-Many avec Disponibilite
     * Un enseignant peut avoir plusieurs créneaux de disponibilité
     */
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Disponibilite> disponibilites = new ArrayList<>();
    
    /**
     * Relation One-to-Many avec EmploiDuTemps
     * Un enseignant peut avoir plusieurs cours dans son emploi du temps
     */
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmploiDuTemps> emploisDuTemps = new ArrayList<>();
    
    /**
     * Relation Many-to-Many avec UE
     * Un enseignant peut enseigner plusieurs UEs
     * Une UE peut être enseignée par plusieurs enseignants
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "enseignant_ue",
        joinColumns = @JoinColumn(name = "enseignant_id", referencedColumnName = "id_user"),
        inverseJoinColumns = @JoinColumn(name = "ue_id")
    )
    private List<UE> ues = new ArrayList<>();

    /**
     * Relation One-to-Many avec Course
     * Un enseignant peut enseigner plusieurs cours
     */
    @OneToMany(mappedBy = "enseignant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();
    
    /**
     * Ajoute une disponibilité à l'enseignant
     */
    public void addDisponibilite(Disponibilite disponibilite) {
        disponibilites.add(disponibilite);
        disponibilite.setEnseignant(this);
    }
    
    /**
     * Supprime une disponibilité de l'enseignant
     */
    public void removeDisponibilite(Disponibilite disponibilite) {
        disponibilites.remove(disponibilite);
        disponibilite.setEnseignant(null);
    }
    
    /**
     * Ajoute un emploi du temps à l'enseignant
     */
    public void addEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.add(emploiDuTemps);
        emploiDuTemps.setEnseignant(this);
    }
    
    /**
     * Supprime un emploi du temps de l'enseignant
     */
    public void removeEmploiDuTemps(EmploiDuTemps emploiDuTemps) {
        emploisDuTemps.remove(emploiDuTemps);
        emploiDuTemps.setEnseignant(null);
    }
    
    /**
     * Ajoute une UE à l'enseignant
     */
    public void addUE(UE ue) {
        if (!ues.contains(ue)) {
            ues.add(ue);
            ue.getEnseignants().add(this);
        }
    }
    
    /**
     * Supprime une UE de l'enseignant
     */
    public void removeUE(UE ue) {
        ues.remove(ue);
        ue.getEnseignants().remove(this);
    }

    /**
     * Ajoute un cours à l'enseignant
     */
    public void addCourse(Course course) {
        courses.add(course);
        course.setEnseignant(this);
    }

    /**
     * Supprime un cours de l'enseignant
     */
    public void removeCourse(Course course) {
        courses.remove(course);
        course.setEnseignant(null);
    }
    
    /**
     * Retourne le nombre total d'UEs enseignées
     */
    public int getNombreUEs() {
        return ues != null ? ues.size() : 0;
    }

    /**
     * Retourne le nombre total de cours enseignés
     */
    public int getNombreCours() {
        return courses != null ? courses.size() : 0;
    }
    
    /**
     * Retourne le nombre total de crédits ECTS enseignés
     */
    public int getTotalCreditsECTS() {
        return ues != null ? ues.stream()
                .mapToInt(UE::getCreditsECTS)
                .sum() : 0;
    }
    
    /**
     * Retourne le nombre total d'heures d'enseignement
     */
    public int getTotalHeuresEnseignement() {
        return courses != null ? courses.stream()
                .mapToInt(Course::getTotalHeures)
                .sum() : 0;
    }

    /**
     * Retourne les classes enseignées par cet enseignant
     */
    public List<Classe> getClassesEnseignees() {
        return courses != null ? courses.stream()
                .map(Course::getClasse)
                .distinct()
                .toList() : new ArrayList<>();
    }

    /**
     * Retourne les cours pour une classe donnée
     */
    public List<Course> getCoursForClasse(Classe classe) {
        return courses != null ? courses.stream()
                .filter(course -> course.getClasse().equals(classe))
                .toList() : new ArrayList<>();
    }
}
