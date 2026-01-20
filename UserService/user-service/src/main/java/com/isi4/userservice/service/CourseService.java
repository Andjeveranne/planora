package com.isi4.userservice.service;

import com.isi4.userservice.dto.CourseDTO;
import com.isi4.userservice.model.Course;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.model.UE;
import com.isi4.userservice.repository.CourseRepository;
import com.isi4.userservice.repository.ClasseRepository;
import com.isi4.userservice.repository.EnseignantRepository;
import com.isi4.userservice.repository.UERepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des Courses
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final ClasseRepository classeRepository;
    private final EnseignantRepository enseignantRepository;
    private final UERepository ueRepository;

    /**
     * Récupère tous les cours actifs
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        log.info("Récupération de tous les cours actifs");
        return courseRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère un cours par son ID
     */
    @Transactional(readOnly = true)
    public Optional<CourseDTO> getCourseById(Long id) {
        if (id == null) {
            log.warn("ID de cours null fourni");
            return Optional.empty();
        }
        log.info("Récupération du cours avec l'ID: {}", id);
        return courseRepository.findById(id)
                .filter(course -> Boolean.TRUE.equals(course.getActive()))
                .map(this::convertToDTO);
    }

    /**
     * Récupère les cours par enseignant
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByEnseignant(String enseignantId) {
        log.info("Récupération des cours pour l'enseignant: {}", enseignantId);
        return courseRepository.findByEnseignantIdAndActiveTrue(enseignantId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les cours par classe
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByClasse(Long classeId) {
        if (classeId == null) {
            throw new IllegalArgumentException("L'ID de la classe ne peut pas être null");
        }
        log.info("Récupération des cours pour la classe: {}", classeId);
        Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'ID: " + classeId));
        
        return courseRepository.findByClasseAndActiveTrue(classe)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les cours par enseignant et classe
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByEnseignantAndClasse(String enseignantId, Long classeId) {
        log.info("Récupération des cours pour l'enseignant {} et la classe {}", enseignantId, classeId);
        return courseRepository.findByEnseignantIdAndClasseIdAndActiveTrue(enseignantId, classeId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les classes enseignées par un enseignant
     */
    @Transactional(readOnly = true)
    public List<Classe> getClassesByEnseignant(String enseignantId) {
        log.info("Récupération des classes enseignées par l'enseignant: {}", enseignantId);
        return courseRepository.findClassesByEnseignant(enseignantId);
    }

    /**
     * Recherche des cours
     */
    @Transactional(readOnly = true)
    public List<CourseDTO> searchCourses(String searchTerm) {
        log.info("Recherche de cours avec le terme: {}", searchTerm);
        return courseRepository.searchCourses(searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouveau cours
     */
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseDTO == null) {
            throw new IllegalArgumentException("Les données du cours ne peuvent pas être null");
        }
        if (courseDTO.getCodeCourse() == null) {
            throw new IllegalArgumentException("Le code du cours ne peut pas être null");
        }
        if (courseDTO.getUeId() == null) {
            throw new IllegalArgumentException("L'ID de l'UE ne peut pas être null");
        }
        if (courseDTO.getEnseignantId() == null) {
            throw new IllegalArgumentException("L'ID de l'enseignant ne peut pas être null");
        }
        if (courseDTO.getClasseId() == null) {
            throw new IllegalArgumentException("L'ID de la classe ne peut pas être null");
        }
        
        log.info("Création d'un nouveau cours: {}", courseDTO.getCodeCourse());
        
        // Vérifier si le code cours existe déjà
        if (courseRepository.existsByCodeCourse(courseDTO.getCodeCourse())) {
            throw new IllegalArgumentException("Un cours avec ce code existe déjà: " + courseDTO.getCodeCourse());
        }

        // Récupérer les entités liées
        UE ue = ueRepository.findById(courseDTO.getUeId())
                .orElseThrow(() -> new IllegalArgumentException("UE non trouvée avec l'ID: " + courseDTO.getUeId()));
        
        Enseignant enseignant = enseignantRepository.findById(courseDTO.getEnseignantId())
                .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé avec l'ID: " + courseDTO.getEnseignantId()));
        
        Classe classe = classeRepository.findById(courseDTO.getClasseId())
                .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'ID: " + courseDTO.getClasseId()));

        Course course = convertToEntity(courseDTO);
        course.setUe(ue);
        course.setEnseignant(enseignant);
        course.setClasse(classe);
        course.setActive(true);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        Course savedCourse = courseRepository.save(course);
        log.info("Cours créé avec succès avec l'ID: {}", savedCourse.getId());
        
        return convertToDTO(savedCourse);
    }

    /**
     * Met à jour un cours existant
     */
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID du cours ne peut pas être null");
        }
        if (courseDTO == null) {
            throw new IllegalArgumentException("Les données du cours ne peuvent pas être null");
        }
        
        log.info("Mise à jour du cours avec l'ID: {}", id);
        
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cours non trouvé avec l'ID: " + id));

        // Vérifier si le code cours existe déjà (excluant le cours actuel)
        if (courseDTO.getCodeCourse() != null && 
            courseRepository.existsByCodeCourseAndIdNot(courseDTO.getCodeCourse(), id)) {
            throw new IllegalArgumentException("Un cours avec ce code existe déjà: " + courseDTO.getCodeCourse());
        }

        // Récupérer les entités liées si elles ont changé
        if (courseDTO.getUeId() != null && !existingCourse.getUe().getId().equals(courseDTO.getUeId())) {
            UE ue = ueRepository.findById(courseDTO.getUeId())
                    .orElseThrow(() -> new IllegalArgumentException("UE non trouvée avec l'ID: " + courseDTO.getUeId()));
            existingCourse.setUe(ue);
        }
        
        if (courseDTO.getEnseignantId() != null && !existingCourse.getEnseignant().getIdUser().equals(courseDTO.getEnseignantId())) {
            Enseignant enseignant = enseignantRepository.findById(courseDTO.getEnseignantId())
                    .orElseThrow(() -> new IllegalArgumentException("Enseignant non trouvé avec l'ID: " + courseDTO.getEnseignantId()));
            existingCourse.setEnseignant(enseignant);
        }
        
        if (courseDTO.getClasseId() != null && !existingCourse.getClasse().getId().equals(courseDTO.getClasseId())) {
            Classe classe = classeRepository.findById(courseDTO.getClasseId())
                    .orElseThrow(() -> new IllegalArgumentException("Classe non trouvée avec l'ID: " + courseDTO.getClasseId()));
            existingCourse.setClasse(classe);
        }

        // Mettre à jour les champs
        existingCourse.setCodeCourse(courseDTO.getCodeCourse());
        existingCourse.setTypeCours(courseDTO.getTypeCours());
        existingCourse.setNombreHeures(courseDTO.getNombreHeures());
        existingCourse.setSemestre(courseDTO.getSemestre());
        existingCourse.setDescription(courseDTO.getDescription());
        existingCourse.setUpdatedAt(LocalDateTime.now());

        Course updatedCourse = courseRepository.save(existingCourse);
        log.info("Cours mis à jour avec succès");
        
        return convertToDTO(updatedCourse);
    }

    /**
     * Supprime un cours (suppression logique)
     */
    public void deleteCourse(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID du cours ne peut pas être null");
        }
        
        log.info("Suppression du cours avec l'ID: {}", id);
        
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cours non trouvé avec l'ID: " + id));

        course.setActive(false);
        course.setUpdatedAt(LocalDateTime.now());
        courseRepository.save(course);
        
        log.info("Cours supprimé avec succès");
    }

    /**
     * Récupère les statistiques des cours
     */
    @Transactional(readOnly = true)
    public CourseStatsDTO getCourseStats() {
        log.info("Récupération des statistiques des cours");
        
        List<Course> allCourses = courseRepository.findByActiveTrue();
        
        CourseStatsDTO stats = new CourseStatsDTO();
        stats.setTotalCourses(allCourses.size());
        stats.setTotalHeures(allCourses.stream().mapToInt(Course::getTotalHeures).sum());
        
        // Statistiques par enseignant
        List<Object[]> statsByEnseignant = courseRepository.countCoursesByEnseignant();
        stats.setStatsByEnseignant(statsByEnseignant.stream()
                .collect(Collectors.toMap(
                        row -> row[0] + " " + row[1],
                        row -> ((Number) row[2]).intValue()
                )));
        
        // Statistiques par classe
        List<Object[]> statsByClasse = courseRepository.countCoursesByClasse();
        stats.setStatsByClasse(statsByClasse.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).intValue()
                )));
        
        return stats;
    }

    /**
     * Convertit une entité Course en DTO
     */
    private CourseDTO convertToDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setCodeCourse(course.getCodeCourse());
        dto.setTypeCours(course.getTypeCours());
        dto.setNombreHeures(course.getNombreHeures());
        dto.setSemestre(course.getSemestre());
        dto.setDescription(course.getDescription());
        dto.setActive(course.getActive());
        dto.setCreatedAt(course.getCreatedAt());
        dto.setUpdatedAt(course.getUpdatedAt());
        
        // Relations
        if (course.getUe() != null) {
            dto.setUeId(course.getUe().getId());
            dto.setUeCode(course.getUe().getCodeUE());
            dto.setUeNom(course.getUe().getNomUE());
        }
        
        if (course.getEnseignant() != null) {
            dto.setEnseignantId(course.getEnseignant().getIdUser());
            dto.setEnseignantNom(course.getEnseignant().getNomUser());
            dto.setEnseignantPrenom(course.getEnseignant().getPrenomUser());
        }
        
        if (course.getClasse() != null) {
            dto.setClasseId(course.getClasse().getId());
            dto.setClasseNom(course.getClasse().getNomClasse());
            dto.setClasseNiveau(course.getClasse().getNiveau());
        }
        
        // Informations calculées
        dto.setNombreCreneaux(course.getNombreCreneaux());
        dto.setHeuresProgrammees(course.getHeuresProgrammees());
        dto.setCompletementProgramme(course.isCompletementProgramme());
        
        // IDs des emplois du temps
        if (course.getEmploisDuTemps() != null) {
            dto.setEmploiDuTempsIds(course.getEmploisDuTemps().stream()
                    .map(edt -> edt.getId())
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }

    /**
     * Convertit un DTO en entité Course
     */
    private Course convertToEntity(CourseDTO dto) {
        Course course = new Course();
        course.setCodeCourse(dto.getCodeCourse());
        course.setTypeCours(dto.getTypeCours());
        course.setNombreHeures(dto.getNombreHeures());
        course.setSemestre(dto.getSemestre());
        course.setDescription(dto.getDescription());
        course.setActive(dto.getActive() != null ? dto.getActive() : true);
        
        return course;
    }

    /**
     * DTO pour les statistiques des cours
     */
    @lombok.Data
    public static class CourseStatsDTO {
        private Integer totalCourses;
        private Integer totalHeures;
        private java.util.Map<String, Integer> statsByEnseignant;
        private java.util.Map<String, Integer> statsByClasse;
    }
}