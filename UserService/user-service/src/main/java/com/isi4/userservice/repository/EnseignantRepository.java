package com.isi4.userservice.repository;

import com.isi4.userservice.model.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnseignantRepository extends JpaRepository<Enseignant, String> {
    List<Enseignant> findByDepartement(String departement);
}
