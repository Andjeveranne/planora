package com.isi4.userservice.repository;

import com.isi4.userservice.model.Disponibilite;
import com.isi4.userservice.model.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Repository pour la gestion des disponibilités des enseignants
 */
@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {

    /**
     * Trouve toutes les disponibilités d'un enseignant
     */
    List<Disponibilite> findByEnseignantOrderByJourSemaineAscHeureDebutAsc(Enseignant enseignant);

    /**
     * Trouve toutes les disponibilités d'un enseignant par son ID
     */
    List<Disponibilite> findByEnseignantIdUserOrderByJourSemaineAscHeureDebutAsc(String enseignantId);

    /**
     * Trouve les disponibilités d'un enseignant pour un jour donné
     */
    List<Disponibilite> findByEnseignantAndJourSemaineOrderByHeureDebutAsc(Enseignant enseignant, DayOfWeek jourSemaine);

    /**
     * Trouve les disponibilités d'un enseignant pour un jour donné par ID
     */
    List<Disponibilite> findByEnseignantIdUserAndJourSemaineOrderByHeureDebutAsc(String enseignantId, DayOfWeek jourSemaine);

    /**
     * Vérifie s'il existe des chevauchements pour un enseignant sur un jour donné
     */
    @Query("SELECT d FROM Disponibilite d WHERE d.enseignant.idUser = :enseignantId " +
           "AND d.jourSemaine = :jourSemaine " +
           "AND d.id != :excludeId " +
           "AND ((d.heureDebut < :heureFin AND d.heureFin > :heureDebut))")
    List<Disponibilite> findChevauchements(@Param("enseignantId") String enseignantId,
                                          @Param("jourSemaine") DayOfWeek jourSemaine,
                                          @Param("heureDebut") LocalTime heureDebut,
                                          @Param("heureFin") LocalTime heureFin,
                                          @Param("excludeId") Long excludeId);

    /**
     * Trouve les enseignants disponibles à un créneau donné
     */
    @Query("SELECT DISTINCT d.enseignant FROM Disponibilite d WHERE d.jourSemaine = :jourSemaine " +
           "AND d.heureDebut <= :heureDebut AND d.heureFin >= :heureFin")
    List<Enseignant> findEnseignantsDisponibles(@Param("jourSemaine") DayOfWeek jourSemaine,
                                               @Param("heureDebut") LocalTime heureDebut,
                                               @Param("heureFin") LocalTime heureFin);

    /**
     * Compte le nombre de créneaux de disponibilité d'un enseignant
     */
    long countByEnseignantIdUser(String enseignantId);

    /**
     * Supprime toutes les disponibilités d'un enseignant
     */
    void deleteByEnseignantIdUser(String enseignantId);

    /**
     * Vérifie si un enseignant a des disponibilités
     */
    boolean existsByEnseignantIdUser(String enseignantId);
}