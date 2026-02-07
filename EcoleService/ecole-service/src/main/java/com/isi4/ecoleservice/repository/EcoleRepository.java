package com.isi4.ecoleservice.repository;

import com.isi4.ecoleservice.entity.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EcoleRepository extends JpaRepository<Ecole, String> {

    Optional<Ecole> findByNomEcole(String nomEcole);

    boolean existsByNomEcole(String nomEcole);

    boolean existsByIdEcole(String idEcole);
}
