package com.isi4.ueservice.config;

import com.isi4.ueservice.entity.UE;
import com.isi4.ueservice.repository.UERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UERepository ueRepository;

    @Override
    public void run(String... args) {
        if (ueRepository.count() == 0) {
            log.info("Initialisation des données des UE...");
            UE ue = UE.builder()
                    .idUE("UE-MATH-001")
                    .codeUE("MATH101")
                    .nomUE("Mathématiques fondamentales")
                    .quantite(60)
                    .description("Cours de mathématiques de base")
                    .nCredit(6)
                    .build();
            ueRepository.save(ue);
            log.info("✓ UE {} créée avec succès", ue.getNomUE());
        }
    }
}
