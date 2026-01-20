package com.isi4.planningservice.repository;

import com.isi4.planningservice.model.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    List<Disponibilite> findByEnseignantId(String enseignantId);

    List<Disponibilite> findByEnseignantIdOrderByDateHeureDebutAsc(String enseignantId);
    
    /**
     * Find availabilities for a teacher within a specific date range
     * Used for overlap detection
     */
    @Query("SELECT d FROM Disponibilite d WHERE d.enseignantId = :enseignantId " +
           "AND d.dateHeureDebut >= :startOfDay AND d.dateHeureDebut <= :endOfDay")
    List<Disponibilite> findByEnseignantIdAndDateHeureDebutBetween(
        @Param("enseignantId") String enseignantId,
        @Param("startOfDay") LocalDateTime startOfDay,
        @Param("endOfDay") LocalDateTime endOfDay
    );
    
    /**
     * Find overlapping availabilities for validation
     */
    @Query("SELECT d FROM Disponibilite d WHERE d.enseignantId = :enseignantId " +
           "AND d.dateHeureDebut < :endTime AND d.dateHeureFin > :startTime")
    List<Disponibilite> findOverlappingAvailabilities(
        @Param("enseignantId") String enseignantId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
