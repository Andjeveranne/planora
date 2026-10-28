package com.isi4.classesservice.config;

import com.isi4.classesservice.entity.Classe;
import com.isi4.classesservice.entity.Filiere;
import com.isi4.classesservice.entity.Specialite;
import com.isi4.classesservice.repository.ClasseRepository;
import com.isi4.classesservice.repository.FiliereRepository;
import com.isi4.classesservice.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final SpecialiteRepository specialiteRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String[] NIVEAUX = {"L1", "L2", "L3", "L4", "L5", "Master1", "Master2"};

    @Override
    public void run(String... args) {
        // Keep schema verification, but never reset or recreate existing data on startup.
        ensureSchema();

        if (filiereRepository.count() == 0 && classeRepository.count() == 0) {
            log.info("Aucune donnée par défaut de départements/classes chargée. Le système démarre vide pour permettre la création manuelle de classes.");
            return;
        }

        log.info("Les données existantes sont conservées; aucun mélange de classes Saint-Jean n'est réinjecté au démarrage.");
    }

    /**
     * Ensures filieres and classes tables exist with the correct schema.
     * Uses raw JDBC so DDL implicit-commits don't interfere with JPA transactions.
     */
    private void ensureSchema() {
        // Create filieres table if absent
        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS filieres (" +
                "  id_filiere  VARCHAR(100) NOT NULL," +
                "  nom         VARCHAR(100) NOT NULL," +
                "  description VARCHAR(255)," +
                "  id_ecole    VARCHAR(100)," +
                "  PRIMARY KEY (id_filiere)" +
                ")"
            );
            Integer schoolColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'filieres' AND COLUMN_NAME = 'id_ecole'",
                Integer.class);
            if (schoolColumnCount == null || schoolColumnCount == 0) {
                jdbcTemplate.execute("ALTER TABLE filieres ADD COLUMN id_ecole VARCHAR(100) NULL");
            }
            List<String> schoolIndependentUniqueIndexes = jdbcTemplate.query(
                "SELECT INDEX_NAME FROM information_schema.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'filieres' " +
                "AND NON_UNIQUE = 0 AND COLUMN_NAME = 'nom' " +
                "GROUP BY INDEX_NAME HAVING COUNT(*) = 1",
                (resultSet, rowNumber) -> resultSet.getString("INDEX_NAME"));
            for (String indexName : schoolIndependentUniqueIndexes) {
                jdbcTemplate.execute("ALTER TABLE filieres DROP INDEX `" +
                    indexName.replace("`", "``") + "`");
            }
            Integer departmentUniqueIndexCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM (" +
                "SELECT INDEX_NAME FROM information_schema.STATISTICS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'filieres' AND NON_UNIQUE = 0 " +
                "GROUP BY INDEX_NAME HAVING COUNT(*) = 2 " +
                "AND SUM(COLUMN_NAME = 'nom') = 1 AND SUM(COLUMN_NAME = 'id_ecole') = 1" +
                ") department_indexes",
                Integer.class);
            if (departmentUniqueIndexCount == null || departmentUniqueIndexCount == 0) {
                jdbcTemplate.execute(
                    "ALTER TABLE filieres ADD CONSTRAINT uk_filieres_nom_ecole UNIQUE (nom, id_ecole)");
            }
            log.info("Table 'filieres' vérifiée/créée.");
        } catch (Exception e) {
            log.error("Erreur création table 'filieres': {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS specialites (" +
                "  id_specialite VARCHAR(100) NOT NULL," +
                "  nom           VARCHAR(100) NOT NULL," +
                "  description   VARCHAR(500)," +
                "  id_filiere    VARCHAR(100) NOT NULL," +
                "  PRIMARY KEY (id_specialite)," +
                "  CONSTRAINT uk_specialites_nom_departement UNIQUE (nom, id_filiere)," +
                "  CONSTRAINT fk_specialites_filiere FOREIGN KEY (id_filiere) " +
                "    REFERENCES filieres(id_filiere)" +
                ")"
            );
            log.info("Table 'specialites' vérifiée/créée.");
        } catch (Exception e) {
            log.error("Erreur création table 'specialites': {}", e.getMessage(), e);
            throw new IllegalStateException("Impossible de préparer la table des spécialités.", e);
        }

        // Check whether classes table has the correct PK column (id_classe)
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "  AND TABLE_NAME = 'classes' " +
                "  AND COLUMN_NAME = 'id_classe'",
                Integer.class);

            if (count == null || count == 0) {
                log.warn("Table 'classes': schéma incorrect ou absente – création du schéma si besoin.");
                jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS classes (" +
                    "  id_classe  VARCHAR(100) NOT NULL," +
                    "  nom_classe VARCHAR(100) NOT NULL," +
                    "  effectifs  INT          NOT NULL," +
                    "  niveau     VARCHAR(50)  NOT NULL," +
                    "  specialite VARCHAR(100)," +
                    "  id_filiere VARCHAR(100)," +
                    "  PRIMARY KEY (id_classe)," +
                    "  UNIQUE KEY uk_classes_nom (nom_classe)," +
                    "  CONSTRAINT fk_classes_filiere" +
                    "    FOREIGN KEY (id_filiere) REFERENCES filieres(id_filiere)" +
                    ")"
                );
                log.info("Table 'classes' créée avec le bon schéma.");
            } else {
                log.info("Table 'classes': schéma correct.");
                Integer specialtyColumnCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'classes' AND COLUMN_NAME = 'specialite'",
                    Integer.class);
                if (specialtyColumnCount == null || specialtyColumnCount == 0) {
                    jdbcTemplate.execute("ALTER TABLE classes ADD COLUMN specialite VARCHAR(100) NULL");
                }
                jdbcTemplate.update(
                    "UPDATE classes SET niveau = CASE niveau " +
                    "WHEN 'ING1' THEN 'L1' WHEN 'ING2' THEN 'L2' WHEN 'ING3' THEN 'L3' " +
                    "WHEN 'ING4' THEN 'L4' WHEN 'ING5' THEN 'L5' ELSE niveau END " +
                    "WHERE niveau IN ('ING1', 'ING2', 'ING3', 'ING4', 'ING5')"
                );
            }
        } catch (Exception e) {
            log.error("Erreur vérification/réparation du schéma de 'classes': {}", e.getMessage());
        }
    }

    /**
     * This project intentionally does not seed sample classes for legacy Saint-Jean campuses.
     * Start with a clean catalog and create the real classes via the app itself.
     */
    private void populateData() {
        log.info("Aucune donnée par défaut de filières/classes n'est créée. Utiliser la gestion des classes de l'application.");
    }

    private void ensureIctUniversityDepartments() {
        log.info("Initialisation des départements par défaut désactivée. Le catalogue est laissé vide pour une création manuelle.");
    }

    private Classe buildClasse(String nom, String niveau, Filiere filiere) {
        return Classe.builder()
                .idClasse("cls-" + nom.toLowerCase().replace(" ", "-"))
                .nomClasse(niveau + " " + filiere.getNom())
                .effectifs(30)
                .niveau(niveau)
                .filiere(filiere)
                .build();
    }

    private void assignDefaultSchool(Filiere filiere) {
        if (filiere != null && filiere.getIdEcole() == null) {
            filiere.setIdEcole("ICT_UNIVERSITY");
            filiereRepository.save(filiere);
        }
    }
}
