package com.isi4.classesservice.config;

import com.isi4.classesservice.entity.Classe;
import com.isi4.classesservice.entity.Filiere;
import com.isi4.classesservice.repository.ClasseRepository;
import com.isi4.classesservice.repository.FiliereRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final String[] NIVEAUX = {"ING1", "ING2", "ING3", "ING4", "ING5"};

    @Override
    public void run(String... args) {
        // Step 1: ensure correct schema via JDBC (DDL causes implicit commit in MySQL,
        // so this must run BEFORE any JPA transaction is opened)
        ensureSchema();

        // Step 2: populate data if needed (each repository.save() has its own transaction)
        populateData();
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
            log.info("Table 'filieres' vérifiée/créée.");
        } catch (Exception e) {
            log.error("Erreur création table 'filieres': {}", e.getMessage());
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
                log.warn("Table 'classes': schéma incorrect ou absente – recréation...");
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
                jdbcTemplate.execute("DROP TABLE IF EXISTS classes");
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
                jdbcTemplate.execute(
                    "CREATE TABLE classes (" +
                    "  id_classe  VARCHAR(100) NOT NULL," +
                    "  nom_classe VARCHAR(100) NOT NULL," +
                    "  effectifs  INT          NOT NULL," +
                    "  niveau     VARCHAR(50)  NOT NULL," +
                    "  id_filiere VARCHAR(100)," +
                    "  PRIMARY KEY (id_classe)," +
                    "  UNIQUE KEY uk_classes_nom (nom_classe)," +
                    "  CONSTRAINT fk_classes_filiere" +
                    "    FOREIGN KEY (id_filiere) REFERENCES filieres(id_filiere)" +
                    ")"
                );
                log.info("Table 'classes' recréée avec le bon schéma.");
            } else {
                log.info("Table 'classes': schéma correct.");
                // Remove any rows with invalid id_filiere (NULL or empty string),
                // which can appear when Hibernate's ddl-auto=update added the column
                // to an existing table populated by another service.
                try {
                    int deleted = jdbcTemplate.update(
                        "DELETE FROM classes WHERE id_filiere IS NULL OR id_filiere = ''"
                    );
                    if (deleted > 0) {
                        log.warn("Supprimé {} classe(s) avec id_filiere invalide (données corrompues).", deleted);
                    }
                } catch (Exception e) {
                    log.error("Erreur nettoyage id_filiere invalides: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Erreur vérification/réparation du schéma de 'classes': {}", e.getMessage());
        }
    }

    /**
     * Populates filieres and classes if they are missing.
     * Each repository.save() runs in its own Spring Data transaction.
     */
    private void populateData() {
        boolean filieresExist = filiereRepository.count() > 0;
        boolean classesExist  = classeRepository.count() > 0;

        if (filieresExist && classesExist) {
            assignDefaultSchool(filiereRepository.findById("fil-isi").orElse(null));
            assignDefaultSchool(filiereRepository.findById("fil-srt").orElse(null));
            assignDefaultSchool(filiereRepository.findById("fil-ge").orElse(null));
            assignDefaultSchool(filiereRepository.findById("fil-gc").orElse(null));
            log.info("Données déjà initialisées ({} filieres, {} classes).",
                     filiereRepository.count(), classeRepository.count());
            return;
        }

        log.info("Initialisation des filières et classes...");

        Filiere isi, srt, ge, gc;

        if (!filieresExist) {
            isi = filiereRepository.save(Filiere.builder()
                    .idFiliere("fil-isi").nom("ISI")
                    .description("Informatique et Systèmes d'Information")
                    .idEcole("SAINT_JEAN_INGENIEUR").build());
            srt = filiereRepository.save(Filiere.builder()
                    .idFiliere("fil-srt").nom("SRT")
                    .description("Systèmes, Réseaux et Télécoms")
                    .idEcole("SAINT_JEAN_INGENIEUR").build());
            ge  = filiereRepository.save(Filiere.builder()
                    .idFiliere("fil-ge").nom("Genie Electrique")
                    .description("Filière Génie Électrique")
                    .idEcole("SAINT_JEAN_INGENIEUR").build());
            gc  = filiereRepository.save(Filiere.builder()
                    .idFiliere("fil-gc").nom("Genie Civil")
                    .description("Filière Génie Civil")
                    .idEcole("SAINT_JEAN_INGENIEUR").build());
            log.info("4 filières créées.");
        } else {
            // Filieres exist but classes might have been wiped by schema repair
            isi = filiereRepository.findById("fil-isi").orElse(null);
            srt = filiereRepository.findById("fil-srt").orElse(null);
            ge  = filiereRepository.findById("fil-ge").orElse(null);
            gc  = filiereRepository.findById("fil-gc").orElse(null);
            assignDefaultSchool(isi);
            assignDefaultSchool(srt);
            assignDefaultSchool(ge);
            assignDefaultSchool(gc);
            log.info("Filières existantes récupérées.");
        }

        if (!classesExist) {
            int count = 0;
            for (String niveau : NIVEAUX) {
                if (isi != null) { classeRepository.save(buildClasse(niveau + " ISI", niveau, isi)); count++; }
                if (srt != null) { classeRepository.save(buildClasse(niveau + " SRT", niveau, srt)); count++; }
                if (ge  != null) { classeRepository.save(buildClasse(niveau + " GE",  niveau, ge));  count++; }
                if (gc  != null) { classeRepository.save(buildClasse(niveau + " GC",  niveau, gc));  count++; }
            }
            log.info("{} classes créées.", count);
        }
    }

    private Classe buildClasse(String nom, String niveau, Filiere filiere) {
        return Classe.builder()
                .idClasse("cls-" + nom.toLowerCase().replace(" ", "-"))
                .nomClasse("ING " + niveau.replace("ING", "") + " " + filiere.getNom())
                .effectifs(30)
                .niveau(niveau)
                .filiere(filiere)
                .build();
    }

    private void assignDefaultSchool(Filiere filiere) {
        if (filiere != null && filiere.getIdEcole() == null) {
            filiere.setIdEcole("SAINT_JEAN_INGENIEUR");
            filiereRepository.save(filiere);
        }
    }
}
