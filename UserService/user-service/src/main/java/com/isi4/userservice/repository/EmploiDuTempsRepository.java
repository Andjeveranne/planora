package com.isi4.userservice.repository;

import com.isi4.userservice.model.EmploiDuTemps;
import com.isi4.userservice.model.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository pour la gestion des emplois du temps
 */
@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    /**
     * Trouve tous les emplois du temps d'un enseignant
     */
    List<EmploiDuTemps> findByEnseignant(Enseignant enseignant);

    /**
     * Trouve tous les emplois du temps d'un enseignant par ID
     */
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignant.idUser = :enseignantId")
    List<EmploiDuTemps> findByEnseignantId(@Param("enseignantId") String enseignantId);

    /**
     * Trouve les emplois du temps d'un enseignant pour une date donnée
     */
    List<EmploiDuTemps> findByEnseignantAndDateJour(Enseignant enseignant, LocalDate dateJour);

    /**
     * Trouve les emplois du temps d'un enseignant entre deux dates
     */
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignant.idUser = :enseignantId AND e.dateJour BETWEEN :dateDebut AND :dateFin ORDER BY e.dateJour, e.heureDebut")
    List<EmploiDuTemps> findByEnseignantIdAndDateJourBetween(
            @Param("enseignantId") String enseignantId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    /**
     * Trouve les emplois du temps pour une salle donnée
     */
    List<EmploiDuTemps> findBySalle(String salle);

    /**
     * Trouve les emplois du temps pour une classe donnée
     */
    List<EmploiDuTemps> findByClasse(String classe);

    /**
     * Trouve les emplois du temps pour une UE donnée
     */
    List<EmploiDuTemps> findByUe(String ue);

    /**
     * Trouve les emplois du temps d'un enseignant ordonnés par date et heure
     */
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignant.idUser = :enseignantId ORDER BY e.dateJour ASC, e.heureDebut ASC")
    List<EmploiDuTemps> findByEnseignantIdOrderByDateJourAscHeureDebutAsc(@Param("enseignantId") String enseignantId);

    /**
     * Vérifie s'il y a des conflits d'horaires pour un enseignant
     */
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignant.idUser = :enseignantId AND e.dateJour = :dateJour AND " +
           "((e.heureDebut < :heureFin AND e.heureFin > :heureDebut))")
    List<EmploiDuTemps> findConflictsForEnseignant(
            @Param("enseignantId") String enseignantId,
            @Param("dateJour") LocalDate dateJour,
            @Param("heureDebut") Integer heureDebut,
            @Param("heureFin") Integer heureFin);

    /**
     * Vérifie s'il y a des conflits de salle
     */
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.salle = :salle AND e.dateJour = :dateJour AND " +
           "((e.heureDebut < :heureFin AND e.heureFin > :heureDebut))")
    List<EmploiDuTemps> findConflictsForSalle(
            @Param("salle") String salle,
            @Param("dateJour") LocalDate dateJour,
            @Param("heureDebut") Integer heureDebut,
            @Param("heureFin") Integer heureFin);

    /**
     * Supprime tous les emplois du temps d'un enseignant
     */
    void deleteByEnseignant(Enseignant enseignant);

    /**
     * Supprime tous les emplois du temps d'un enseignant par ID
     */
    @Query("DELETE FROM EmploiDuTemps e WHERE e.enseignant.idUser = :enseignantId")
    void deleteByEnseignantId(@Param("enseignantId") String enseignantId);
}