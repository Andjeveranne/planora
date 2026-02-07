package com.isi4.ueservice.repository;

import com.isi4.ueservice.entity.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UERepository extends JpaRepository<UE, String> {

    Optional<UE> findByCodeUE(String codeUE);

    boolean existsByCodeUE(String codeUE);

    boolean existsByIdUE(String idUE);
}
