package com.isi4.ecoleservice.config;

import com.isi4.ecoleservice.entity.Ecole;
import com.isi4.ecoleservice.repository.EcoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise une école par défaut (référencée par SalleService et autres services).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EcoleRepository ecoleRepository;

    @Override
    public void run(String... args) {
        if (ecoleRepository.count() == 0) {
            log.info("Initialisation des données des écoles...");
            Ecole saintJean = Ecole.builder()
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .nomEcole("Saint Jean Ingénieur")
                    .build();
            ecoleRepository.save(saintJean);
            log.info("✓ École {} créée avec succès", saintJean.getNomEcole());
        }
    }
}
