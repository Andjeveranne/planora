package com.isi4.userservice.repository;

import com.isi4.userservice.entity.EnseignantUE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnseignantUERepository extends JpaRepository<EnseignantUE, Long> {
    java.util.Optional<EnseignantUE> findByUeIdAndIdEcole(Long ueId, String idEcole);
    
    List<EnseignantUE> findByEnseignantId(String enseignantId);
    
    List<EnseignantUE> findByUeId(Long ueId);
    
    Optional<EnseignantUE> findByEnseignantIdAndUeId(String enseignantId, Long ueId);

    Optional<EnseignantUE> findByEnseignantIdAndUeIdAndIdEcole(
            String enseignantId, Long ueId, String idEcole);
    
    List<EnseignantUE> findByEnseignantIdAndPeutCMTrue(String enseignantId);
    
    List<EnseignantUE> findByEnseignantIdAndPeutTDTrue(String enseignantId);
    
    List<EnseignantUE> findByEnseignantIdAndPeutTPTrue(String enseignantId);
}