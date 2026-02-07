package com.isi4.salleservice.config;

import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    
    private final SalleRepository salleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (salleRepository.count() == 0) {
            log.info("Initialisation des données des salles de Saint Jean...");
            
            List<Salle> sallesSaintJean = Arrays.asList(
                // Étage 3 Bâtiment 2
                Salle.builder()
                    .nomSalle("Salle Présentation Prepavogt")
                    .type(TypeSalle.SALLE_CONFERENCE)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(50) // Capacité inconnue, valeur par défaut
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 304 ING4SRT")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(32)
                    .capaciteExam(16)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 306 ING4ISI")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(36)
                    .capaciteExam(18)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 303 Dédoublement")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 305 APPA")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(50) // Capacité inconnue, valeur par défaut
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 308 ING4ISIFR")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(42)
                    .capaciteExam(21)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 307 Dédoublement")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 310 Dédoublement")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 312 Dédoublement")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 309 Dédoublement")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 324 APPA")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(50) // Capacité inconnue, valeur par défaut
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 312 ING5IL")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 313 ING2FR")
                    .type(TypeSalle.AMPHITHEATRE)
                    .localisation("Étage 3 Bâtiment 2")
                    .capacite(110)
                    .capaciteExam(55)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                // Étage 2 Bâtiment 2
                Salle.builder()
                    .nomSalle("Salle 215 Licence II")
                    .type(TypeSalle.AMPHITHEATRE)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(116)
                    .capaciteExam(58)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 216 Licence I Anglophone")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(22)
                    .capaciteExam(11)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 213 Licence I Francophone")
                    .type(TypeSalle.AMPHITHEATRE)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(112)
                    .capaciteExam(56)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 214 Master I MIA")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(18)
                    .capaciteExam(9)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 207A Master I BI-DA")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(16)
                    .capaciteExam(8)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 208 Licence III MTQ")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(32)
                    .capaciteExam(16)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 205 Licence III MC")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(14)
                    .capaciteExam(7)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 201B Master I AMF")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(32)
                    .capaciteExam(16)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 202B Licence III MF")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(38)
                    .capaciteExam(19)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 203B Master I MBFA")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 2 Bâtiment 2")
                    .capacite(50) // Capacité inconnue, valeur par défaut
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                // Étage 1 Bâtiment 2
                Salle.builder()
                    .nomSalle("Salle 113 ING1EN")
                    .type(TypeSalle.AMPHITHEATRE)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(110)
                    .capaciteExam(55)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 111B ING1FR")
                    .type(TypeSalle.AMPHITHEATRE)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(110)
                    .capaciteExam(55)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 114 ING4GC")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(24)
                    .capaciteExam(12)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 112B ING3GC")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(32)
                    .capaciteExam(16)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 110B ING3GE")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(20)
                    .capaciteExam(10)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 108 ING2EN")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(50)
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 106 ING1BFR")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(50)
                    .capaciteExam(25)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 101 Dédouble")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage 1 Bâtiment 2")
                    .capacite(20)
                    .capaciteExam(10)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                // RDC Bâtiment 2
                Salle.builder()
                    .nomSalle("Salle 001A IGEA 2")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(46)
                    .capaciteExam(23)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 002 IGEA 1")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(40)
                    .capaciteExam(20)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 004 IGC 2")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(42)
                    .capaciteExam(21)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 006A MF")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(54)
                    .capaciteExam(27)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 006B")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 007B TD")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 008A")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 008B")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 009B")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 013B SPH I")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 009 SPH II")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment 2")
                    .capacite(30) // Capacité par défaut
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                // Étage II Bâtiment I
                Salle.builder()
                    .nomSalle("Salle 207 Bibliothèque")
                    .type(TypeSalle.BIBLIOTHEQUE)
                    .localisation("Étage II Bâtiment I")
                    .capacite(100) // Capacité inconnue, valeur par défaut
                    .capaciteExam(50)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 203B ING3ISIFR")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage II Bâtiment I")
                    .capacite(56)
                    .capaciteExam(28)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 201 INGEISIE")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("Étage II Bâtiment I")
                    .capacite(44)
                    .capaciteExam(22)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 2051 Laboratoire")
                    .type(TypeSalle.LABORATOIRE)
                    .localisation("Étage II Bâtiment I")
                    .capacite(30)
                    .capaciteExam(15)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                // RDC Bâtiment I
                Salle.builder()
                    .nomSalle("Salle 009B Laboratoire")
                    .type(TypeSalle.LABORATOIRE)
                    .localisation("RDC Bâtiment I")
                    .capacite(35)
                    .capaciteExam(17)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 005 CPGE1 A")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment I")
                    .capacite(18)
                    .capaciteExam(9)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 003B CPGE1 B")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment I")
                    .capacite(18)
                    .capaciteExam(9)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 003A CPGE2")
                    .type(TypeSalle.SALLE_COURS)
                    .localisation("RDC Bâtiment I")
                    .capacite(17)
                    .capaciteExam(8)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build(),
                    
                Salle.builder()
                    .nomSalle("Salle 001 Salle de présentation")
                    .type(TypeSalle.SALLE_CONFERENCE)
                    .localisation("RDC Bâtiment I")
                    .capacite(15)
                    .capaciteExam(7)
                    .idEcole("SAINT_JEAN_INGENIEUR")
                    .statut(StatutSalle.DISPONIBLE)
                    .build()
            );
            
            salleRepository.saveAll(sallesSaintJean);
            log.info("✓ {} salles de Saint Jean créées avec succès", sallesSaintJean.size());
        } else {
            log.info("Les données de salles existent déjà, initialisation ignorée");
        }
    }
}