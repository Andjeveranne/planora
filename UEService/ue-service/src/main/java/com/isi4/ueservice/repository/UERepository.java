package com.isi4.ueservice.repository;

import com.isi4.ueservice.entity.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UERepository extends JpaRepository<UE, Long> {

    Optional<UE> findByCodeUE(String codeUE);

    boolean existsByCodeUE(String codeUE);

    List<UE> findByIdFiliere(String idFiliere);

    List<UE> findByNiveau(String niveau);

    List<UE> findByIdFiliereAndNiveau(String idFiliere, String niveau);
}
