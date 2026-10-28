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
        log.info("Initialisation des UE désactivée : aucun jeu d'UE legacy n'est injecté au démarrage.");
    }

    @Transactional
    public void cleanAndInsert() {
        log.info("Aucune donnée par défaut d'UE n'est créée. Utiliser la gestion des UE de l'application.");
    }

    private void repairLegacyDemoUEs() {
        log.info("Réparation des UE legacy désactivée.");
    }
}
