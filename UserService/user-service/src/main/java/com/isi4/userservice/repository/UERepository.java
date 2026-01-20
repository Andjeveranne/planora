package com.isi4.userservice.repository;

import com.isi4.userservice.model.UE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des Unités d'Enseignement (UE)
 */
@Repository
public interface UERepository extends JpaRepository<UE, Long> {

    /**
     * Trouve une UE par son code
     */
    Optional<UE> findByCodeUE(String codeUE);

    /**
     * Trouve toutes les UEs actives
     */
    List<UE> findByActiveTrue();

    /**
     * Trouve toutes les UEs inactives
     */
    List<UE> findByActiveFalse();

    /**
     * Trouve les UEs par semestre
     */
    List<UE> findBySemestreAndActiveTrue(String semestre);

    /**
     * Trouve les UEs par niveau
     */
    List<UE> findByNiveauAndActiveTrue(String niveau);

    /**
     * Trouve les UEs par semestre et niveau
     */
    List<UE> findBySemestreAndNiveauAndActiveTrue(String semestre, String niveau);

    /**
     * Recherche les UEs par nom (recherche partielle, insensible à la casse)
     */
    @Query("SELECT u FROM UE u WHERE LOWER(u.nomUE) LIKE LOWER(CONCAT('%', :nom, '%')) AND u.active = true")
    List<UE> findByNomUEContainingIgnoreCaseAndActiveTrue(@Param("nom") String nom);

    /**
     * Recherche les UEs par code (recherche partielle, insensible à la casse)
     */
    @Query("SELECT u FROM UE u WHERE LOWER(u.codeUE) LIKE LOWER(CONCAT('%', :code, '%')) AND u.active = true")
    List<UE> findByCodeUEContainingIgnoreCaseAndActiveTrue(@Param("code") String code);

    /**
     * Trouve les UEs enseignées par un enseignant spécifique
     */
    @Query("SELECT u FROM UE u JOIN u.enseignants e WHERE e.idUser = :enseignantId AND u.active = true")
    List<UE> findByEnseignantId(@Param("enseignantId") String enseignantId);

    /**
     * Trouve les UEs avec un nombre de crédits ECTS spécifique
     */
    List<UE> findByCreditsECTSAndActiveTrue(Integer creditsECTS);

    /**
     * Trouve les UEs avec un nombre de crédits ECTS dans une plage
     */
    @Query("SELECT u FROM UE u WHERE u.creditsECTS BETWEEN :minCredits AND :maxCredits AND u.active = true")
    List<UE> findByCreditsECTSBetweenAndActiveTrue(@Param("minCredits") Integer minCredits, 
                                                   @Param("maxCredits") Integer maxCredits);

    /**
     * Trouve les UEs avec un nombre total d'heures dans une plage
     */
    @Query("SELECT u FROM UE u WHERE (u.heuresCM + u.heuresTD + u.heuresTP) BETWEEN :minHeures AND :maxHeures AND u.active = true")
    List<UE> findByTotalHeuresBetweenAndActiveTrue(@Param("minHeures") Integer minHeures, 
                                                   @Param("maxHeures") Integer maxHeures);

    /**
     * Compte le nombre d'UEs par semestre
     */
    @Query("SELECT u.semestre, COUNT(u) FROM UE u WHERE u.active = true GROUP BY u.semestre")
    List<Object[]> countUEsBySemestre();

    /**
     * Compte le nombre d'UEs par niveau
     */
    @Query("SELECT u.niveau, COUNT(u) FROM UE u WHERE u.active = true GROUP BY u.niveau")
    List<Object[]> countUEsByNiveau();

    /**
     * Trouve les UEs sans enseignants assignés
     */
    @Query("SELECT u FROM UE u WHERE u.enseignants IS EMPTY AND u.active = true")
    List<UE> findUEsWithoutEnseignants();

    /**
     * Trouve les UEs avec le plus d'enseignants
     */
    @Query("SELECT u FROM UE u WHERE u.active = true ORDER BY SIZE(u.enseignants) DESC")
    List<UE> findUEsOrderByNombreEnseignantsDesc();

    /**
     * Recherche globale dans les UEs (nom, code, description)
     */
    @Query("SELECT u FROM UE u WHERE u.active = true AND " +
           "(LOWER(u.nomUE) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.codeUE) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<UE> searchUEs(@Param("searchTerm") String searchTerm);

    /**
     * Vérifie si un code UE existe déjà
     */
    boolean existsByCodeUE(String codeUE);

    /**
     * Vérifie si un code UE existe déjà (en excluant un ID spécifique pour les mises à jour)
     */
    @Query("SELECT COUNT(u) > 0 FROM UE u WHERE u.codeUE = :codeUE AND u.id != :excludeId")
    boolean existsByCodeUEAndIdNot(@Param("codeUE") String codeUE, @Param("excludeId") Long excludeId);

    /**
     * Trouve les UEs les plus récentes
     */
    @Query("SELECT u FROM UE u WHERE u.active = true ORDER BY u.createdAt DESC")
    List<UE> findRecentUEs();

    /**
     * Statistiques des UEs
     */
    @Query("SELECT " +
           "COUNT(u) as totalUEs, " +
           "SUM(u.creditsECTS) as totalCredits, " +
           "AVG(u.creditsECTS) as avgCredits, " +
           "SUM(u.heuresCM + u.heuresTD + u.heuresTP) as totalHeures " +
           "FROM UE u WHERE u.active = true")
    Object[] getUEStatistics();
}