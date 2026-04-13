package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.UEClasse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UEClasseRepository extends JpaRepository<UEClasse, Long> {
    
    List<UEClasse> findByUeId(Long ueId);
    
    List<UEClasse> findByClasseId(String classeId);
    
    boolean existsByUeIdAndClasseId(Long ueId, String classeId);
}