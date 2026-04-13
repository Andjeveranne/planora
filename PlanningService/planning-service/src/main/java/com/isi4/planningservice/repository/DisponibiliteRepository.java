package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    List<Disponibilite> findByEnseignantIdOrderByDateHeureDebutAsc(String enseignantId);
    List<Disponibilite> findByEnseignantIdAndDateHeureDebutBetween(String enseignantId, LocalDateTime start, LocalDateTime end);
    void deleteByEnseignantId(String enseignantId);

    @Query("SELECT DISTINCT d.enseignantId FROM Disponibilite d")
    List<String> findDistinctEnseignantIds();

    long countByEnseignantId(String enseignantId);
}
