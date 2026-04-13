package com.isi4.classesservice.repository;

import com.isi4.classesservice.entity.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, String> {
    Optional<Filiere> findByNom(String nom);
    boolean existsByNom(String nom);
}
