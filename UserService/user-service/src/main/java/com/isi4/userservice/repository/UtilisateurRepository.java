package com.isi4.userservice.repository;

import com.isi4.userservice.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdUserNot(String email, String idUser);
    List<Utilisateur> findByRole(String role);
}
