package com.isi4.classesservice.repository;

import com.isi4.classesservice.entity.Specialite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpecialiteRepository extends JpaRepository<Specialite, String> {
    List<Specialite> findByIdFiliere(String idFiliere);
    boolean existsByNomAndIdFiliere(String nom, String idFiliere);
    boolean existsByNomAndIdFiliereAndIdSpecialiteNot(String nom, String idFiliere, String idSpecialite);
}
