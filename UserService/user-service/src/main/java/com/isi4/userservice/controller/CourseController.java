package com.isi4.userservice.controller;

import com.isi4.userservice.dto.CourseDTO;
import com.isi4.userservice.model.Classe;
import com.isi4.userservice.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des Courses
 */
@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService courseService;

    /**
     * Récupère tous les cours
     */
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        log.info("GET /api/courses - Récupération de tous les cours");
        try {
            List<CourseDTO> courses = courseService.getAllCourses();
            log.info("Récupération réussie de {} cours", courses.size());
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des cours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un cours par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        log.info("GET /api/courses/{} - Récupération du cours", id);
        try {
            return courseService.getCourseById(id)
                    .map(course -> {
                        log.info("Cours trouvé: {}", course.getCodeCourse());
                        return ResponseEntity.ok(course);
                    })
                    .orElseGet(() -> {
                        log.warn("Cours non trouvé avec l'ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du cours avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les cours par enseignant
     */
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByEnseignant(@PathVariable String enseignantId) {
        log.info("GET /api/courses/enseignant/{} - Récupération des cours par enseignant", enseignantId);
        try {
            List<CourseDTO> courses = courseService.getCoursesByEnseignant(enseignantId);
            log.info("Récupération réussie de {} cours pour l'enseignant {}", courses.size(), enseignantId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des cours par enseignant: {}", enseignantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les cours par classe
     */
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByClasse(@PathVariable Long classeId) {
        log.info("GET /api/courses/classe/{} - Récupération des cours par classe", classeId);
        try {
            List<CourseDTO> courses = courseService.getCoursesByClasse(classeId);
            log.info("Récupération réussie de {} cours pour la classe {}", courses.size(), classeId);
            return ResponseEntity.ok(courses);
        } catch (IllegalArgumentException e) {
            log.warn("Classe non trouvée: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des cours par classe: {}", classeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les cours par enseignant et classe
     */
    @GetMapping("/enseignant/{enseignantId}/classe/{classeId}")
    public ResponseEntity<List<CourseDTO>> getCoursesByEnseignantAndClasse(
            @PathVariable String enseignantId,
            @PathVariable Long classeId) {
        log.info("GET /api/courses/enseignant/{}/classe/{} - Récupération des cours par enseignant et classe", 
                enseignantId, classeId);
        try {
            List<CourseDTO> courses = courseService.getCoursesByEnseignantAndClasse(enseignantId, classeId);
            log.info("Récupération réussie de {} cours pour l'enseignant {} et la classe {}", 
                    courses.size(), enseignantId, classeId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des cours par enseignant {} et classe {}", 
                    enseignantId, classeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les classes enseignées par un enseignant
     */
    @GetMapping("/enseignant/{enseignantId}/classes")
    public ResponseEntity<List<Classe>> getClassesByEnseignant(@PathVariable String enseignantId) {
        log.info("GET /api/courses/enseignant/{}/classes - Récupération des classes par enseignant", enseignantId);
        try {
            List<Classe> classes = courseService.getClassesByEnseignant(enseignantId);
            log.info("Récupération réussie de {} classes pour l'enseignant {}", classes.size(), enseignantId);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes par enseignant: {}", enseignantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des cours
     */
    @GetMapping("/search")
    public ResponseEntity<List<CourseDTO>> searchCourses(@RequestParam String q) {
        log.info("GET /api/courses/search?q={} - Recherche de cours", q);
        try {
            List<CourseDTO> courses = courseService.searchCourses(q);
            log.info("Recherche réussie: {} cours trouvés", courses.size());
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de cours avec le terme: {}", q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crée un nouveau cours
     */
    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        log.info("POST /api/courses - Création d'un nouveau cours: {}", courseDTO.getCodeCourse());
        try {
            CourseDTO createdCourse = courseService.createCourse(courseDTO);
            log.info("Cours créé avec succès avec l'ID: {}", createdCourse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCourse);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la création du cours: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la création du cours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour un cours existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO courseDTO) {
        log.info("PUT /api/courses/{} - Mise à jour du cours", id);
        try {
            CourseDTO updatedCourse = courseService.updateCourse(id, courseDTO);
            log.info("Cours mis à jour avec succès");
            return ResponseEntity.ok(updatedCourse);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la mise à jour du cours: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du cours avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime un cours
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        log.info("DELETE /api/courses/{} - Suppression du cours", id);
        try {
            courseService.deleteCourse(id);
            log.info("Cours supprimé avec succès");
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Cours non trouvé pour suppression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du cours avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les statistiques des cours
     */
    @GetMapping("/stats")
    public ResponseEntity<CourseService.CourseStatsDTO> getCourseStats() {
        log.info("GET /api/courses/stats - Récupération des statistiques des cours");
        try {
            CourseService.CourseStatsDTO stats = courseService.getCourseStats();
            log.info("Statistiques récupérées avec succès");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des cours", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}