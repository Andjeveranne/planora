package com.isi4.planningservice.repository;

import com.isi4.planningservice.model.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps, Long> {

    List<EmploiDuTemps> findByEnseignantId(String enseignantId);

    List<EmploiDuTemps> findByEnseignantIdAndDateJourBetween(
            String enseignantId, 
            LocalDate dateDebut, 
            LocalDate dateFin
    );

    @Query("SELECT e FROM EmploiDuTemps e WHERE e.enseignantId = :enseignantId AND e.dateJour = :dateJour")
    List<EmploiDuTemps> findByEnseignantIdAndDateJour(
            @Param("enseignantId") String enseignantId, 
            @Param("dateJour") LocalDate dateJour
    );

    void deleteByEnseignantId(String enseignantId);

    boolean existsByEnseignantIdAndDateJourAndHeureDebut(
            String enseignantId, 
            LocalDate dateJour, 
            Integer heureDebut
    );
}
