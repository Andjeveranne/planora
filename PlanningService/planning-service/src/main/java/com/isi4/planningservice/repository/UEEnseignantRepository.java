package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.UEEnseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UEEnseignantRepository extends JpaRepository<UEEnseignant, Long> {
    
    List<UEEnseignant> findByUeId(Long ueId);
    
    List<UEEnseignant> findByEnseignantId(String enseignantId);
    
    boolean existsByUeIdAndEnseignantId(Long ueId, String enseignantId);
}