package com.isi4.userservice.service;

import com.isi4.userservice.dto.EmploiDuTempsDTO;
import com.isi4.userservice.model.*;
import com.isi4.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour la génération intelligente d'emplois du temps
 * basée sur les disponibilités réelles des enseignants et leurs cours assignés
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimetableGenerationService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final CourseRepository courseRepository;
    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final EnseignantRepository enseignantRepository;
    private final ClasseRepository classeRepository;

    // Salles disponibles par défaut
    private static final String[] SALLES_DISPONIBLES = {
        "Salle 101", "Salle 102", "Salle 103", "Salle 201", "Salle 202", "Salle 203",
        "Labo Info 1", "Labo Info 2", "Labo Info 3", "Amphi A", "Amphi B", "Salle 305"
    };

    /**
     * Génère un emploi du temps intelligent pour un enseignant
     * basé sur ses disponibilités et ses cours assignés
     */
    @Transactional
    public List<EmploiDuTempsDTO> genererEmploiDuTempsIntelligent(String enseignantId, int nombreSemaines) {
        log.info("Génération d'emploi du temps intelligent pour l'enseignant {} sur {} semaines", enseignantId, nombreSemaines);

        // Vérifier que l'enseignant existe
        Enseignant enseignant = enseignantRepository.findById(enseignantId)
                .orElseThrow(() -> new RuntimeException("Enseignant non trouvé: " + enseignantId));

        // Récupérer les disponibilités de l'enseignant
        List<Disponibilite> disponibilites = disponibiliteRepository.findByEnseignantIdUserOrderByJourSemaineAscHeureDebutAsc(enseignantId);
        if (disponibilites.isEmpty()) {
            log.warn("Aucune disponibilité trouvée pour l'enseignant {}", enseignantId);
            return new ArrayList<>();
        }

        // Récupérer les cours assignés à l'enseignant
        List<Course> coursesAssignes = courseRepository.findByEnseignantAndActiveTrue(enseignant);
        if (coursesAssignes.isEmpty()) {
            log.warn("Aucun cours assigné trouvé pour l'enseignant {}", enseignantId);
            return new ArrayList<>();
        }

        // Supprimer les emplois du temps futurs existants
        supprimerEmploisDuTempsFuturs(enseignantId);

        // Générer les emplois du temps
        List<EmploiDuTemps> nouveauxEmplois = new ArrayList<>();
        LocalDate dateDebut = LocalDate.now().plusDays(1); // Commencer demain

        // Créer un planificateur de cours
        CoursePlanner planner = new CoursePlanner(coursesAssignes, disponibilites);

        for (int semaine = 0; semaine < nombreSemaines; semaine++) {
            LocalDate debutSemaine = dateDebut.plusWeeks(semaine);
            List<EmploiDuTemps> emploisSemaine = genererEmploisPourSemaine(enseignant, planner, debutSemaine);
            nouveauxEmplois.addAll(emploisSemaine);
        }

        // Sauvegarder tous les nouveaux emplois
        List<EmploiDuTemps> emploisSauvegardes = emploiDuTempsRepository.saveAll(nouveauxEmplois);
        
        log.info("Génération terminée: {} créneaux créés pour l'enseignant {}", emploisSauvegardes.size(), enseignantId);

        return emploisSauvegardes.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Génère les emplois du temps pour une semaine donnée
     */
    private List<EmploiDuTemps> genererEmploisPourSemaine(Enseignant enseignant, CoursePlanner planner, LocalDate debutSemaine) {
        List<EmploiDuTemps> emploisSemaine = new ArrayList<>();
        Random random = new Random();

        // Pour chaque jour de la semaine
        for (int jour = 0; jour < 7; jour++) {
            LocalDate dateJour = debutSemaine.plusDays(jour);
            DayOfWeek dayOfWeek = dateJour.getDayOfWeek();

            // Récupérer les disponibilités pour ce jour
            List<Disponibilite> disposDuJour = planner.getDisponibilitesParJour(dayOfWeek);

            for (Disponibilite dispo : disposDuJour) {
                // Calculer les heures précises du créneau
                LocalTime heureDebut = dispo.getHeureDebut();
                LocalTime heureFin = dispo.getHeureFin();
                
                // Créer des créneaux d'1 heure pour toute la période de disponibilité
                LocalTime currentHeure = heureDebut;
                while (currentHeure.isBefore(heureFin)) {
                    LocalTime nextHeure = currentHeure.plusHours(1);
                    
                    // Si le créneau suivant dépasse la fin, ajuster à la fin
                    if (nextHeure.isAfter(heureFin)) {
                        nextHeure = heureFin;
                    }
                    
                    // Essayer de placer un cours dans ce créneau d'1 heure
                    Optional<Course> coursOptional = planner.obtenirProchainCours();
                    
                    if (coursOptional.isPresent()) {
                        Course cours = coursOptional.get();

                        // Créer l'emploi du temps pour ce créneau d'1 heure
                        EmploiDuTemps emploi = new EmploiDuTemps();
                        emploi.setEnseignant(enseignant);
                        emploi.setDateJour(dateJour);
                        emploi.setJour(dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH));
                        emploi.setHeureDebut(currentHeure.getHour());
                        emploi.setHeureFin(nextHeure.getHour());
                        emploi.setUe(cours.getUe().getCodeUE() + " - " + cours.getUe().getNomUE());
                        emploi.setClasse(cours.getClasse().getNomClasse());
                        emploi.setSalle(SALLES_DISPONIBLES[random.nextInt(SALLES_DISPONIBLES.length)]);
                        emploi.setTypeCours(cours.getTypeCours());
                        emploi.setCourse(cours);
                        
                        LocalDateTime now = LocalDateTime.now();
                        emploi.setCreatedAt(now);
                        emploi.setUpdatedAt(now);

                        emploisSemaine.add(emploi);
                        
                        // Marquer le cours comme utilisé
                        planner.marquerCoursUtilise(cours);
                        
                        log.debug("Créneau créé: {} {} de {}h à {}h - {} pour {}", 
                                dateJour, dayOfWeek, currentHeure.getHour(), nextHeure.getHour(), 
                                cours.getUe().getCodeUE(), cours.getClasse().getNomClasse());
                    }
                    
                    // Passer au créneau suivant
                    currentHeure = nextHeure;
                }
            }
        }

        return emploisSemaine;
    }

    /**
     * Supprime les emplois du temps futurs d'un enseignant
     */
    private void supprimerEmploisDuTempsFuturs(String enseignantId) {
        LocalDate aujourdhui = LocalDate.now();
        List<EmploiDuTemps> emploisExistants = emploiDuTempsRepository.findByEnseignantIdOrderByDateJourAscHeureDebutAsc(enseignantId);
        
        List<EmploiDuTemps> emploisFuturs = emploisExistants.stream()
                .filter(e -> e.getDateJour().isAfter(aujourdhui))
                .collect(Collectors.toList());
        
        if (!emploisFuturs.isEmpty()) {
            emploiDuTempsRepository.deleteAll(emploisFuturs);
            log.info("Suppression de {} emplois du temps futurs pour l'enseignant {}", emploisFuturs.size(), enseignantId);
        }
    }

    /**
     * Convertit une entité EmploiDuTemps en DTO
     */
    private EmploiDuTempsDTO convertToDTO(EmploiDuTemps emploi) {
        EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
        dto.setId(emploi.getId());
        dto.setEnseignantId(emploi.getEnseignant().getIdUser());
        dto.setDateJour(emploi.getDateJour());
        dto.setJour(emploi.getJour());
        dto.setHeureDebut(emploi.getHeureDebut());
        dto.setHeureFin(emploi.getHeureFin());
        dto.setUe(emploi.getUe());
        dto.setClasse(emploi.getClasse());
        dto.setSalle(emploi.getSalle());
        dto.setTypeCours(emploi.getTypeCours());
        dto.setCreatedAt(emploi.getCreatedAt());
        dto.setUpdatedAt(emploi.getUpdatedAt());
        return dto;
    }

    /**
     * Classe interne pour planifier les cours de manière intelligente
     */
    private static class CoursePlanner {
        private final List<Course> coursesDisponibles;
        private final Map<DayOfWeek, List<Disponibilite>> disponibilitesParJour;
        private final Set<Long> coursUtilises;
        private int indexCours;

        public CoursePlanner(List<Course> courses, List<Disponibilite> disponibilites) {
            this.coursesDisponibles = new ArrayList<>(courses);
            this.coursUtilises = new HashSet<>();
            this.indexCours = 0;
            
            // Grouper les disponibilités par jour
            this.disponibilitesParJour = disponibilites.stream()
                    .collect(Collectors.groupingBy(Disponibilite::getJourSemaine));
        }

        public List<Disponibilite> getDisponibilitesParJour(DayOfWeek jour) {
            return disponibilitesParJour.getOrDefault(jour, new ArrayList<>());
        }

        public Optional<Course> obtenirProchainCours() {
            // Essayer de trouver un cours non encore utilisé
            for (int i = 0; i < coursesDisponibles.size(); i++) {
                Course cours = coursesDisponibles.get((indexCours + i) % coursesDisponibles.size());
                if (!coursUtilises.contains(cours.getId())) {
                    indexCours = (indexCours + i + 1) % coursesDisponibles.size();
                    return Optional.of(cours);
                }
            }
            
            // Si tous les cours ont été utilisés, recommencer (permet la répétition)
            if (!coursesDisponibles.isEmpty()) {
                coursUtilises.clear();
                Course cours = coursesDisponibles.get(indexCours % coursesDisponibles.size());
                indexCours = (indexCours + 1) % coursesDisponibles.size();
                return Optional.of(cours);
            }
            
            return Optional.empty();
        }

        public void marquerCoursUtilise(Course cours) {
            coursUtilises.add(cours.getId());
        }
    }
}