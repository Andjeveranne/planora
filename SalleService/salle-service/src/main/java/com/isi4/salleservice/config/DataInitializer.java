package com.isi4.salleservice.config;

import com.isi4.salleservice.entity.Salle;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialise les salles du répertoire des classes de Saint Jean (plannoradb).
 * Quatre écoles : Saint Jean Ingénieur, Saint Jean Management, Prépa Vogt, CPGE.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final int CAP_INCONNU = 50;
    private static final int CAP_EXAM_INCONNU = 25;
    private static final int CAP_VIDE = 30;
    private static final int CAP_EXAM_VIDE = 15;
    
    private final SalleRepository salleRepository;
    
    @Override
    public void run(String... args) throws Exception {
        if (salleRepository.count() == 0) {
            log.info("Initialisation des salles du répertoire Saint Jean (plannoradb)...");

            List<Salle> salles = List.of(
                    // ----- Étage 3 Bâtiment 2 -----
                    salle("Salle Présentation Prepavogt", TypeSalle.SALLE_CONFERENCE, "Étage 3 Bâtiment 2", CAP_INCONNU, CAP_EXAM_INCONNU, "PREPA_VOGT"),
                    salle("Salle 304 ING4SRT", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 32, 16, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 306 ING4ISI", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 36, 18, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 303 Dédoublement", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 305 APPA", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", CAP_INCONNU, CAP_EXAM_INCONNU, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 308 ING4ISIFR", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 42, 21, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 307 Dédoublement", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 310 Dédoublement", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 312 Dédoublement", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 309 Dédoublement", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 324 APPA", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", CAP_INCONNU, CAP_EXAM_INCONNU, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 312 ING5IL", TypeSalle.SALLE_COURS, "Étage 3 Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 313 ING2FR", TypeSalle.AMPHITHEATRE, "Étage 3 Bâtiment 2", 110, 55, "SAINT_JEAN_INGENIEUR"),
                    // ----- Étage 2 Bâtiment 2 (Management) -----
                    salle("Salle 215 Licence II", TypeSalle.AMPHITHEATRE, "Étage 2 Bâtiment 2", 116, 58, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 216 Licence I Anglophone", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 22, 11, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 213 Licence I Francophone", TypeSalle.AMPHITHEATRE, "Étage 2 Bâtiment 2", 112, 56, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 214 Master I MIA", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 18, 9, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 207A Master I BI-DA", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 16, 8, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 208 Licence III MTQ", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 32, 16, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 205 Licence III MC", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 14, 7, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 201B Master I AMF", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 32, 16, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 202B Licence III MF", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", 38, 19, "SAINT_JEAN_MANAGEMENT"),
                    salle("Salle 203B Master I MBFA", TypeSalle.SALLE_COURS, "Étage 2 Bâtiment 2", CAP_INCONNU, CAP_EXAM_INCONNU, "SAINT_JEAN_MANAGEMENT"),
                    // ----- Étage 1 Bâtiment 2 -----
                    salle("Salle 113 ING1EN", TypeSalle.AMPHITHEATRE, "Étage 1 Bâtiment 2", 110, 55, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 111B ING1FR", TypeSalle.AMPHITHEATRE, "Étage 1 Bâtiment 2", 110, 55, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 114 ING4GC", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 24, 12, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 112B ING3GC", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 32, 16, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 110B ING3GE", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 20, 10, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 108 ING2EN", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 50, 25, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 106 ING1BFR", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 50, 25, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 101 Dédouble", TypeSalle.SALLE_COURS, "Étage 1 Bâtiment 2", 20, 10, "SAINT_JEAN_INGENIEUR"),
                    // ----- RDC Bâtiment 2 -----
                    salle("Salle 001A IGEA 2", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", 46, 23, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 002 IGEA 1", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", 40, 20, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 004 IGC 2", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", 42, 21, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 006A MF", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", 54, 27, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 006B", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 007B TD", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 008A", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 008B", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 009B", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 013B SPH I", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 009 SPH II", TypeSalle.SALLE_COURS, "RDC Bâtiment 2", CAP_VIDE, CAP_EXAM_VIDE, "SAINT_JEAN_INGENIEUR"),
                    // ----- Étage II Bâtiment I -----
                    salle("Salle 207 Bibliothèque", TypeSalle.BIBLIOTHEQUE, "Étage II Bâtiment I", CAP_INCONNU, CAP_EXAM_INCONNU, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 203B ING3ISIFR", TypeSalle.SALLE_COURS, "Étage II Bâtiment I", 56, 28, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 201 INGEISIE", TypeSalle.SALLE_COURS, "Étage II Bâtiment I", 44, 22, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 2051 Laboratoire", TypeSalle.LABORATOIRE, "Étage II Bâtiment I", 30, 15, "SAINT_JEAN_INGENIEUR"),
                    // ----- RDC Bâtiment I -----
                    salle("Salle 009B Laboratoire", TypeSalle.LABORATOIRE, "RDC Bâtiment I", 35, 17, "SAINT_JEAN_INGENIEUR"),
                    salle("Salle 005 CPGE1 A", TypeSalle.SALLE_COURS, "RDC Bâtiment I", 18, 9, "CPGE"),
                    salle("Salle 003B CPGE1 B", TypeSalle.SALLE_COURS, "RDC Bâtiment I", 18, 9, "CPGE"),
                    salle("Salle 003A CPGE2", TypeSalle.SALLE_COURS, "RDC Bâtiment I", 17, 8, "CPGE"),
                    salle("Salle 001 Salle de présentation", TypeSalle.SALLE_CONFERENCE, "RDC Bâtiment I", 15, 7, "SAINT_JEAN_INGENIEUR")
            );

            salleRepository.saveAll(salles);
            log.info("✓ {} salles créées avec succès (plannoradb)", salles.size());
        } else {
            log.info("Les données de salles existent déjà, initialisation ignorée");
        }
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
