package com.isi4.ueservice.config;

import com.isi4.ueservice.entity.UE;
import com.isi4.ueservice.repository.UERepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UERepository ueRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        cleanAndInsert();
    }

    @Transactional
    public void cleanAndInsert() {
        log.info("Nettoyage et initialisation de la table UE...");

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // Vider courses si elle existe
        try {
            jdbcTemplate.execute("DELETE FROM courses");
        } catch (Exception ignored) {
            log.debug("Table courses inexistante, ignoré.");
        }

        jdbcTemplate.execute("DELETE FROM ues");

        // Rendre les colonnes héritées du UserService optionnelles
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN active TINYINT(1) DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN semestre VARCHAR(10) DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN niveau VARCHAR(50) DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN heures_cm INT DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN heures_td INT DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN heures_tp INT DEFAULT NULL");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE ues MODIFY COLUMN credits_ects INT DEFAULT NULL");
        } catch (Exception ignored) {}

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("Table UE vidée. Insertion des données initiales...");

        List<UE> ues = List.of(
            UE.builder().idUE("ue-info-algo").codeUE("INFO101")
                .nomUE("Algorithmique et structures de données")
                .description("Introduction aux algorithmes fondamentaux et aux structures de données classiques.")
                .nombreCredit(6).nombreHeur(60).build(),
            UE.builder().idUE("ue-info-poo").codeUE("INFO102")
                .nomUE("Programmation orientée objet")
                .description("Concepts de la POO : classes, héritage, polymorphisme, encapsulation.")
                .nombreCredit(5).nombreHeur(45).build(),
            UE.builder().idUE("ue-math-analyse").codeUE("MATH101")
                .nomUE("Analyse mathématique")
                .description("Suites, séries, fonctions de plusieurs variables, intégrales.")
                .nombreCredit(6).nombreHeur(60).build(),
            UE.builder().idUE("ue-math-proba").codeUE("MATH102")
                .nomUE("Probabilités et statistiques")
                .description("Variables aléatoires, lois de probabilité, tests statistiques.")
                .nombreCredit(4).nombreHeur(40).build(),
            UE.builder().idUE("ue-reseau-base").codeUE("RES101")
                .nomUE("Réseaux informatiques")
                .description("Modèle OSI, protocoles TCP/IP, routage et commutation.")
                .nombreCredit(4).nombreHeur(40).build()
        );

        ueRepository.saveAll(ues);
        log.info("✓ {} UEs insérées avec succès.", ues.size());
    }
}
