package com.isi4.userservice.repository;

import com.isi4.userservice.model.Course;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour l'entité Course
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Trouve un cours par son code
     */
    Optional<Course> findByCodeCourse(String codeCourse);

    /**
     * Trouve tous les cours actifs
     */
    List<Course> findByActiveTrue();

    /**
     * Trouve les cours par enseignant
     */
    List<Course> findByEnseignantAndActiveTrue(Enseignant enseignant);

    /**
     * Trouve les cours par enseignant ID
     */
    @Query("SELECT c FROM Course c WHERE c.enseignant.idUser = :enseignantId AND c.active = true")
    List<Course> findByEnseignantIdAndActiveTrue(@Param("enseignantId") String enseignantId);

    /**
     * Trouve les cours par classe
     */
    List<Course> findByClasseAndActiveTrue(Classe classe);

    /**
     * Trouve les cours par UE
     */
    List<Course> findByUeAndActiveTrue(UE ue);

    /**
     * Trouve les cours par type
     */
    List<Course> findByTypeCoursAndActiveTrue(String typeCours);

    /**
     * Trouve les cours par semestre
     */
    List<Course> findBySemestreAndActiveTrue(String semestre);

    /**
     * Trouve les cours par enseignant et classe
     */
    List<Course> findByEnseignantAndClasseAndActiveTrue(Enseignant enseignant, Classe classe);

    /**
     * Trouve les cours par enseignant ID et classe ID
     */
    @Query("SELECT c FROM Course c WHERE c.enseignant.idUser = :enseignantId AND c.classe.id = :classeId AND c.active = true")
    List<Course> findByEnseignantIdAndClasseIdAndActiveTrue(@Param("enseignantId") String enseignantId, @Param("classeId") Long classeId);

    /**
     * Trouve les cours par UE et enseignant
     */
    List<Course> findByUeAndEnseignantAndActiveTrue(UE ue, Enseignant enseignant);

    /**
     * Vérifie si un cours existe par son code
     */
    boolean existsByCodeCourse(String codeCourse);

    /**
     * Vérifie si un cours existe par son code (excluant un ID spécifique)
     */
    boolean existsByCodeCourseAndIdNot(String codeCourse, Long id);

    /**
     * Trouve les cours avec leurs emplois du temps
     */
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.emploisDuTemps WHERE c.active = true")
    List<Course> findCoursesWithEmploisDuTemps();

    /**
     * Trouve les cours sans emplois du temps
     */
    @Query("SELECT c FROM Course c WHERE c.active = true AND c.emploisDuTemps IS EMPTY")
    List<Course> findCoursesWithoutEmploisDuTemps();

    /**
     * Compte le nombre de cours par enseignant
     */
    @Query("SELECT e.nomUser, e.prenomUser, COUNT(c) FROM Course c JOIN c.enseignant e WHERE c.active = true GROUP BY e.idUser, e.nomUser, e.prenomUser")
    List<Object[]> countCoursesByEnseignant();

    /**
     * Compte le nombre de cours par classe
     */
    @Query("SELECT cl.nomClasse, COUNT(c) FROM Course c JOIN c.classe cl WHERE c.active = true GROUP BY cl.id, cl.nomClasse")
    List<Object[]> countCoursesByClasse();

    /**
     * Compte le nombre de cours par UE
     */
    @Query("SELECT u.codeUE, u.nomUE, COUNT(c) FROM Course c JOIN c.ue u WHERE c.active = true GROUP BY u.id, u.codeUE, u.nomUE")
    List<Object[]> countCoursesByUE();

    /**
     * Recherche les cours par code, type ou description
     */
    @Query("SELECT c FROM Course c WHERE c.active = true AND " +
           "(LOWER(c.codeCourse) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.typeCours) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Course> searchCourses(@Param("searchTerm") String searchTerm);

    /**
     * Trouve les cours d'un enseignant avec leurs statistiques
     */
    @Query("SELECT c, COUNT(edt), SUM(edt.heureFin - edt.heureDebut) FROM Course c " +
           "LEFT JOIN c.emploisDuTemps edt " +
           "WHERE c.enseignant.idUser = :enseignantId AND c.active = true " +
           "GROUP BY c")
    List<Object[]> findCoursesWithStatsByEnseignant(@Param("enseignantId") String enseignantId);

    /**
     * Trouve les cours d'une classe avec leurs statistiques
     */
    @Query("SELECT c, COUNT(edt), SUM(edt.heureFin - edt.heureDebut) FROM Course c " +
           "LEFT JOIN c.emploisDuTemps edt " +
           "WHERE c.classe.id = :classeId AND c.active = true " +
           "GROUP BY c")
    List<Object[]> findCoursesWithStatsByClasse(@Param("classeId") Long classeId);

    /**
     * Trouve les classes enseignées par un enseignant
     */
    @Query("SELECT DISTINCT c.classe FROM Course c WHERE c.enseignant.idUser = :enseignantId AND c.active = true")
    List<Classe> findClassesByEnseignant(@Param("enseignantId") String enseignantId);

    /**
     * Trouve les UEs enseignées par un enseignant dans une classe
     */
    @Query("SELECT DISTINCT c.ue FROM Course c WHERE c.enseignant.idUser = :enseignantId AND c.classe.id = :classeId AND c.active = true")
    List<UE> findUEsByEnseignantAndClasse(@Param("enseignantId") String enseignantId, @Param("classeId") Long classeId);
}