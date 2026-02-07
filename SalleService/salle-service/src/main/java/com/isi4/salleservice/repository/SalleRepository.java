package com.isi4.salleservice.repository;

import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalleRepository extends JpaRepository<Salle, String> {
    
    // Rechercher par école
    List<Salle> findByIdEcole(String idEcole);
    
    // Rechercher par type
    List<Salle> findByType(TypeSalle type);
    
    // Rechercher par statut
    List<Salle> findByStatut(StatutSalle statut);
    
    // Rechercher par école et type
    List<Salle> findByIdEcoleAndType(String idEcole, TypeSalle type);
    
    // Rechercher par école et statut
    List<Salle> findByIdEcoleAndStatut(String idEcole, StatutSalle statut);
    
    // Rechercher par capacité minimale
    List<Salle> findByCapaciteGreaterThanEqual(Integer capacite);
    
    // Rechercher par capacité d'examen minimale
    List<Salle> findByCapaciteExamGreaterThanEqual(Integer capaciteExam);
    
    // Rechercher par localisation (contient)
    List<Salle> findByLocalisationContainingIgnoreCase(String localisation);
    
    // Rechercher salles disponibles par école avec capacité minimale
    @Query("SELECT s FROM Salle s WHERE s.idEcole = :idEcole AND s.statut = 'DISPONIBLE' AND s.capacite >= :capaciteMin")
    List<Salle> findSallesDisponibles(@Param("idEcole") String idEcole, @Param("capaciteMin") Integer capaciteMin);
    
    // Vérifier si une salle existe par nom et école
    boolean existsByNomSalleAndIdEcole(String nomSalle, String idEcole);
}