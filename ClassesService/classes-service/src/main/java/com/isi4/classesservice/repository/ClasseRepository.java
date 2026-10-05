package com.isi4.classesservice.repository;

import com.isi4.classesservice.entity.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, String> {
    List<Classe> findByFiliereIdFiliere(String idFiliere);

    List<Classe> findByFiliereIdEcole(String idEcole);
    List<Classe> findByNiveau(String niveau);
    boolean existsByNomClasse(String nomClasse);
}
