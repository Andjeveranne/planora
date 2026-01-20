package com.isi4.userservice.controller;

import com.isi4.userservice.dto.ClasseDTO;
import com.isi4.userservice.service.ClasseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des Classes
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ClasseController {

    private final ClasseService classeService;

    /**
     * Récupère toutes les classes
     */
    @GetMapping
    public ResponseEntity<List<ClasseDTO>> getAllClasses() {
        log.info("GET /api/classes - Récupération de toutes les classes");
        try {
            List<ClasseDTO> classes = classeService.getAllClasses();
            log.info("Récupération réussie de {} classes", classes.size());
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère une classe par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClasseDTO> getClasseById(@PathVariable Long id) {
        log.info("GET /api/classes/{} - Récupération de la classe", id);
        try {
            return classeService.getClasseById(id)
                    .map(classe -> {
                        log.info("Classe trouvée: {}", classe.getNomClasse());
                        return ResponseEntity.ok(classe);
                    })
                    .orElseGet(() -> {
                        log.warn("Classe non trouvée avec l'ID: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la classe avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les classes par niveau
     */
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<ClasseDTO>> getClassesByNiveau(@PathVariable String niveau) {
        log.info("GET /api/classes/niveau/{} - Récupération des classes par niveau", niveau);
        try {
            List<ClasseDTO> classes = classeService.getClassesByNiveau(niveau);
            log.info("Récupération réussie de {} classes pour le niveau {}", classes.size(), niveau);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes par niveau: {}", niveau, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les classes par filière
     */
    @GetMapping("/filiere/{filiere}")
    public ResponseEntity<List<ClasseDTO>> getClassesByFiliere(@PathVariable String filiere) {
        log.info("GET /api/classes/filiere/{} - Récupération des classes par filière", filiere);
        try {
            List<ClasseDTO> classes = classeService.getClassesByFiliere(filiere);
            log.info("Récupération réussie de {} classes pour la filière {}", classes.size(), filiere);
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes par filière: {}", filiere, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Recherche des classes
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClasseDTO>> searchClasses(@RequestParam String q) {
        log.info("GET /api/classes/search?q={} - Recherche de classes", q);
        try {
            List<ClasseDTO> classes = classeService.searchClasses(q);
            log.info("Recherche réussie: {} classes trouvées", classes.size());
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            log.error("Erreur lors de la recherche de classes avec le terme: {}", q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Crée une nouvelle classe
     */
    @PostMapping
    public ResponseEntity<ClasseDTO> createClasse(@Valid @RequestBody ClasseDTO classeDTO) {
        log.info("POST /api/classes - Création d'une nouvelle classe: {}", classeDTO.getNomClasse());
        try {
            ClasseDTO createdClasse = classeService.createClasse(classeDTO);
            log.info("Classe créée avec succès avec l'ID: {}", createdClasse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClasse);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la création de la classe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la création de la classe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Met à jour une classe existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClasseDTO> updateClasse(@PathVariable Long id, @Valid @RequestBody ClasseDTO classeDTO) {
        log.info("PUT /api/classes/{} - Mise à jour de la classe", id);
        try {
            ClasseDTO updatedClasse = classeService.updateClasse(id, classeDTO);
            log.info("Classe mise à jour avec succès");
            return ResponseEntity.ok(updatedClasse);
        } catch (IllegalArgumentException e) {
            log.warn("Erreur de validation lors de la mise à jour de la classe: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour de la classe avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprime une classe
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClasse(@PathVariable Long id) {
        log.info("DELETE /api/classes/{} - Suppression de la classe", id);
        try {
            classeService.deleteClasse(id);
            log.info("Classe supprimée avec succès");
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Classe non trouvée pour suppression: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la classe avec l'ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les statistiques des classes
     */
    @GetMapping("/stats")
    public ResponseEntity<ClasseService.ClasseStatsDTO> getClasseStats() {
        log.info("GET /api/classes/stats - Récupération des statistiques des classes");
        try {
            ClasseService.ClasseStatsDTO stats = classeService.getClasseStats();
            log.info("Statistiques récupérées avec succès");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des statistiques des classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}