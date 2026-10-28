package com.isi4.userservice.repository;

import com.isi4.userservice.model.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, String> {
    Optional<Etudiant> findByEmail(String email);
    List<Etudiant> findByIdEcole(String idEcole);
    List<Etudiant> findByRole(String role);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdUserNot(String email, String idUser);
}
