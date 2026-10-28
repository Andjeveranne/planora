package com.isi4.salleservice.config;

import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * The application starts with an empty room catalog.
 * Users create their own rooms from the admin interface.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final SalleRepository salleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Initialisation des salles désactivée : aucun jeu de salles Saint-Jean legacy n'est injecté au démarrage.");
    }

    private static Salle salle(String nom, TypeSalle type, String localisation, int capacite, int capaciteExam, String idEcole) {
        return Salle.builder()
                .nomSalle(nom)
                .type(type)
                .localisation(localisation)
                .capacite(capacite)
                .capaciteExam(capaciteExam)
                .idEcole(idEcole)
                .statut(StatutSalle.DISPONIBLE)
                .build();
    }
}
