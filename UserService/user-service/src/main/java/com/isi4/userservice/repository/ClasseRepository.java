package com.isi4.userservice.repository;

import com.isi4.userservice.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Classe
 */
@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {

    /**
     * Trouve une classe par son nom
     */
    Optional<Classe> findByNomClasse(String nomClasse);

    /**
     * Trouve toutes les classes actives
     */
    List<Classe> findByActiveTrue();

    /**
     * Trouve les classes par niveau
     */
    List<Classe> findByNiveauAndActiveTrue(String niveau);

    /**
     * Trouve les classes par filière
     */
    List<Classe> findByFiliereAndActiveTrue(String filiere);

    /**
     * Trouve les classes par année académique
     */
    List<Classe> findByAnneeAcademiqueAndActiveTrue(String anneeAcademique);

    /**
     * Trouve les classes par niveau et filière
     */
    List<Classe> findByNiveauAndFiliereAndActiveTrue(String niveau, String filiere);

    /**
     * Vérifie si une classe existe par son nom
     */
    boolean existsByNomClasse(String nomClasse);

    /**
     * Vérifie si une classe existe par son nom (excluant un ID spécifique)
     */
    boolean existsByNomClasseAndIdNot(String nomClasse, Long id);

    /**
     * Trouve les classes avec des cours
     */
    @Query("SELECT DISTINCT c FROM Classe c JOIN c.courses co WHERE c.active = true")
    List<Classe> findClassesWithCourses();

    /**
     * Trouve les classes sans cours
     */
    @Query("SELECT c FROM Classe c WHERE c.active = true AND c.courses IS EMPTY")
    List<Classe> findClassesWithoutCourses();

    /**
     * Compte le nombre de classes par niveau
     */
    @Query("SELECT c.niveau, COUNT(c) FROM Classe c WHERE c.active = true GROUP BY c.niveau")
    List<Object[]> countClassesByNiveau();

    /**
     * Compte le nombre de classes par filière
     */
    @Query("SELECT c.filiere, COUNT(c) FROM Classe c WHERE c.active = true GROUP BY c.filiere")
    List<Object[]> countClassesByFiliere();

    /**
     * Trouve les classes avec le nombre total d'étudiants
     */
    @Query("SELECT c, SUM(c.nombreEtudiants) FROM Classe c WHERE c.active = true GROUP BY c")
    List<Object[]> findClassesWithTotalStudents();

    /**
     * Recherche les classes par nom, niveau ou filière
     */
    @Query("SELECT c FROM Classe c WHERE c.active = true AND " +
           "(LOWER(c.nomClasse) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.niveau) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.filiere) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Classe> searchClasses(@Param("searchTerm") String searchTerm);

    /**
     * Trouve les classes d'une année académique spécifique avec leurs statistiques
     */
    @Query("SELECT c, COUNT(co), SUM(co.nombreHeures) FROM Classe c " +
           "LEFT JOIN c.courses co " +
           "WHERE c.anneeAcademique = :anneeAcademique AND c.active = true " +
           "GROUP BY c")
    List<Object[]> findClassesWithStatsByAnneeAcademique(@Param("anneeAcademique") String anneeAcademique);
}