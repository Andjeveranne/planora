package com.isi4.planningservice.repository;

import com.isi4.planningservice.model.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibiliteRepository extends JpaRepository<Disponibilite, Long> {
    List<Disponibilite> findByEnseignantId(String enseignantId);

    List<Disponibilite> findByEnseignantIdOrderByDateHeureDebutAsc(String enseignantId);
}
