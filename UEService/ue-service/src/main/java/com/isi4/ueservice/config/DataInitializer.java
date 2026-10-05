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
import java.util.ArrayList;

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
        log.info("Vérification et initialisation des unités d'enseignement...");

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

        if (ueRepository.count() > 0) {
            repairLegacyDemoUEs();
            log.info("{} UE(s) existantes conservées.", ueRepository.count());
            return;
        }

        String[][] filieres = {
                {"fil-isi", "ISI"}, {"fil-srt", "SRT"},
                {"fil-ge", "Génie électrique"}, {"fil-gc", "Génie civil"}
        };
        List<UE> ues = new ArrayList<>();
        for (String[] filiere : filieres) {
            for (int niveau = 1; niveau <= 5; niveau++) {
                String niveauCode = "ING" + niveau;
                String code = filiere[1].toUpperCase().replaceAll("[^A-Z]", "") + "-" + niveauCode + "-CORE";
                ues.add(UE.builder()
                        .idUE("ue-" + filiere[0] + "-" + niveauCode.toLowerCase())
                        .codeUE(code)
                        .nomUE("Unité fondamentale " + filiere[1] + " " + niveauCode)
                        .description("UE de démonstration modifiable pour la filière " + filiere[1] + ".")
                        .nombreCredit(4)
                        .nombreHeur(4)
                        .heuresCM(2)
                        .heuresTD(2)
                        .heuresTP(0)
                        .niveau(niveauCode)
                        .idFiliere(filiere[0])
                        .nomFiliere(filiere[1])
                        .build());
            }
        }

        ueRepository.saveAll(ues);
        log.info("{} UEs de démonstration planifiables créées; les données existantes ne sont jamais supprimées.", ues.size());
    }

    private void repairLegacyDemoUEs() {
        for (UE ue : ueRepository.findAll()) {
            String code = ue.getCodeUE();
            String filiereId = switch (code) {
                case "INFO101", "INFO102", "MATH101", "MATH102" -> "fil-isi";
                case "RES101" -> "fil-srt";
                default -> null;
            };
            if (filiereId == null) {
                continue;
            }

            boolean changed = false;
            if (ue.getIdFiliere() == null) {
                ue.setIdFiliere(filiereId);
                changed = true;
            }
            if (ue.getNiveau() == null) {
                ue.setNiveau("ING1");
                changed = true;
            }
            if (ue.getHeuresCM() == null) {
                ue.setHeuresCM(2);
                changed = true;
            }
            if (ue.getHeuresTD() == null) {
                ue.setHeuresTD(2);
                changed = true;
            }
            if (ue.getHeuresTP() == null) {
                ue.setHeuresTP(0);
                changed = true;
            }
            if (ue.getNomFiliere() == null) {
                ue.setNomFiliere("fil-isi".equals(filiereId) ? "ISI" : "SRT");
                changed = true;
            }
            if (changed) {
                ueRepository.save(ue);
                log.info("Métadonnées de planification réparées pour l'UE {}.", code);
            }
        }
    }
}
