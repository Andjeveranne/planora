package com.isi4.planningservice.config;

import com.isi4.planningservice.entity.UEClasse;
import com.isi4.planningservice.entity.UEEnseignant;
import com.isi4.planningservice.repository.UEClasseRepository;
import com.isi4.planningservice.repository.UEEnseignantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UEClasseRepository ueClasseRepository;
    private final UEEnseignantRepository ueEnseignantRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeUEClasses();
        initializeUEEnseignants();
    }

    private void initializeUEClasses() {
        if (ueClasseRepository.count() == 0) {
            log.info("Initialisation des relations UE-Classe...");
            
            // Relation UE 1 - Classe 1
            UEClasse ueClasse1 = new UEClasse();
            ueClasse1.setUeId(1L);
            ueClasse1.setClasseId("classe-1");
            ueClasse1.setHeuresCM(20);
            ueClasse1.setHeuresTD(10);
            ueClasse1.setHeuresTP(10);
            ueClasseRepository.save(ueClasse1);

            // Relation UE 2 - Classe 1
            UEClasse ueClasse2 = new UEClasse();
            ueClasse2.setUeId(2L);
            ueClasse2.setClasseId("classe-1");
            ueClasse2.setHeuresCM(15);
            ueClasse2.setHeuresTD(15);
            ueClasse2.setHeuresTP(0);
            ueClasseRepository.save(ueClasse2);

            // Relation UE 1 - Classe 2
            UEClasse ueClasse3 = new UEClasse();
            ueClasse3.setUeId(1L);
            ueClasse3.setClasseId("classe-2");
            ueClasse3.setHeuresCM(20);
            ueClasse3.setHeuresTD(10);
            ueClasse3.setHeuresTP(10);
            ueClasseRepository.save(ueClasse3);

            log.info("Relations UE-Classe initialisées avec succès");
        }
    }

    private void initializeUEEnseignants() {
        if (ueEnseignantRepository.count() == 0) {
            log.info("Initialisation des relations UE-Enseignant...");
            
            // Enseignant 1 peut faire CM et TD pour UE 1
            UEEnseignant ueEnseignant1 = new UEEnseignant();
            ueEnseignant1.setUeId(1L);
            ueEnseignant1.setEnseignantId("enseignant-1");
            ueEnseignant1.setPeutCM(true);
            ueEnseignant1.setPeutTD(true);
            ueEnseignant1.setPeutTP(false);
            ueEnseignantRepository.save(ueEnseignant1);

            // Enseignant 2 peut faire CM, TD et TP pour UE 2
            UEEnseignant ueEnseignant2 = new UEEnseignant();
            ueEnseignant2.setUeId(2L);
            ueEnseignant2.setEnseignantId("enseignant-2");
            ueEnseignant2.setPeutCM(true);
            ueEnseignant2.setPeutTD(true);
            ueEnseignant2.setPeutTP(true);
            ueEnseignantRepository.save(ueEnseignant2);

            // Enseignant 1 peut faire TD et TP pour UE 2
            UEEnseignant ueEnseignant3 = new UEEnseignant();
            ueEnseignant3.setUeId(2L);
            ueEnseignant3.setEnseignantId("enseignant-1");
            ueEnseignant3.setPeutCM(false);
            ueEnseignant3.setPeutTD(true);
            ueEnseignant3.setPeutTP(true);
            ueEnseignantRepository.save(ueEnseignant3);

            log.info("Relations UE-Enseignant initialisées avec succès");
        }
    }
}