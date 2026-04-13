package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {
    
    List<EmploiDuTemps> findByClasseIdOrderByDateHeureDebutAsc(String classeId);
    
    List<EmploiDuTemps> findByEnseignantIdOrderByDateHeureDebutAsc(String enseignantId);
    
    List<EmploiDuTemps> findBySalleIdOrderByDateHeureDebutAsc(String salleId);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.classeId = :classeId AND e.semaine = :semaine ORDER BY e.jourSemaine, e.dateHeureDebut")
    List<EmploiDuTemps> findByClasseIdAndSemaine(@Param("classeId") String classeId, @Param("semaine") Integer semaine);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignantId = :enseignantId AND e.semaine = :semaine ORDER BY e.jourSemaine, e.dateHeureDebut")
    List<EmploiDuTemps> findByEnseignantIdAndSemaine(@Param("enseignantId") String enseignantId, @Param("semaine") Integer semaine);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.salleId = :salleId AND " +
           "((e.dateHeureDebut <= :fin AND e.dateHeureFin > :debut))")
    List<EmploiDuTemps> findConflictingSalle(@Param("salleId") String salleId, 
                                           @Param("debut") LocalDateTime debut, 
                                           @Param("fin") LocalDateTime fin);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignantId = :enseignantId AND " +
           "((e.dateHeureDebut <= :fin AND e.dateHeureFin > :debut))")
    List<EmploiDuTemps> findConflictingEnseignant(@Param("enseignantId") String enseignantId, 
                                                 @Param("debut") LocalDateTime debut, 
                                                 @Param("fin") LocalDateTime fin);
    
    @Query("SELECT e FROM EmploiDuTemps e WHERE e.classeId = :classeId AND " +
           "((e.dateHeureDebut <= :fin AND e.dateHeureFin > :debut))")
    List<EmploiDuTemps> findConflictingClasse(@Param("classeId") String classeId,
                                             @Param("debut") LocalDateTime debut,
                                             @Param("fin") LocalDateTime fin);

    @Modifying
    @Query("DELETE FROM EmploiDuTemps e WHERE e.semaine = :semaine")
    int deleteBySemaine(@Param("semaine") Integer semaine);
}