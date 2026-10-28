package com.isi4.ecoleservice.config;

import com.isi4.ecoleservice.entity.Ecole;
import com.isi4.ecoleservice.repository.EcoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialise les écoles et universités de référence pour la génération d'emplois du temps.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final EcoleRepository ecoleRepository;

    @Override
    public void run(String... args) {
        log.info("Les écoles par défaut Saint-Jean/CPGE/Prépa sont désactivées pour ne pas réinjecter de catalogues legacy au démarrage.");
    }
}
