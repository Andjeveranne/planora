package com.isi4.userservice.controller;

import com.isi4.userservice.dto.UEDTO;
import com.isi4.userservice.service.UEService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des Unités d'Enseignement (UE)
 */
@RestController
@RequestMapping("/api/ues")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UEController {

    private final UEService ueService;

    /**
     * Récupère toutes les UEs actives
     */
    @GetMapping
    public ResponseEntity<List<UEDTO>> getAllUEs() {
        log.info("GET /api/ues - Récupération de toutes les UEs");
        List<UEDTO> ues = ueService.getAllActiveUEs();
        return ResponseEntity.ok(ues);
    }

    /**
     * Récupère toutes les UEs avec pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<UEDTO>> getAllUEsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nomUE") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        log.info("GET /api/ues/paginated - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                page, size, sortBy, sortDir);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UEDTO> ues = ueService.getAllActiveUEs(pageable);
        return ResponseEntity.ok(ues);
    }

    /**
     * Récupère une UE par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UEDTO> getUEById(@PathVariable Long id) {
        log.info("GET /api/ues/{}", id);
        UEDTO ue = ueService.getUEById(id);
        return ResponseEntity.ok(ue);
    }

    /**
     * Récupère une UE par son code
     */
    @GetMapping("/code/{codeUE}")
    public ResponseEntity<UEDTO> getUEByCode(@PathVariable String codeUE) {
        log.info("GET /api/ues/code/{}", codeUE);
        UEDTO ue = ueService.getUEByCode(codeUE);
        return ResponseEntity.ok(ue);
    }

    /**
     * Crée une nouvelle UE
     */
    @PostMapping
    public ResponseEntity<UEDTO> createUE(@Valid @RequestBody UEDTO ueDTO) {
        log.info("POST /api/ues - Création d'une UE: {}", ueDTO.getCodeUE());
        UEDTO createdUE = ueService.createUE(ueDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUE);
    }

    /**
     * Met à jour une UE existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<UEDTO> updateUE(@PathVariable Long id, @Valid @RequestBody UEDTO ueDTO) {
        log.info("PUT /api/ues/{} - Mise à jour de l'UE", id);
        UEDTO updatedUE = ueService.updateUE(id, ueDTO);
        return ResponseEntity.ok(updatedUE);
    }

    /**
     * Supprime une UE (suppression logique)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUE(@PathVariable Long id) {
        log.info("DELETE /api/ues/{} - Suppression de l'UE", id);
        ueService.deleteUE(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Restaure une UE supprimée
     */
    @PutMapping("/{id}/restore")
    public ResponseEntity<UEDTO> restoreUE(@PathVariable Long id) {
        log.info("PUT /api/ues/{}/restore - Restauration de l'UE", id);
        UEDTO restoredUE = ueService.restoreUE(id);
        return ResponseEntity.ok(restoredUE);
    }

    /**
     * Recherche des UEs
     */
    @GetMapping("/search")
    public ResponseEntity<List<UEDTO>> searchUEs(@RequestParam String q) {
        log.info("GET /api/ues/search?q={}", q);
        List<UEDTO> ues = ueService.searchUEs(q);
        return ResponseEntity.ok(ues);
    }

    /**
     * Récupère les UEs par semestre
     */
    @GetMapping("/semestre/{semestre}")
    public ResponseEntity<List<UEDTO>> getUEsBySemestre(@PathVariable String semestre) {
        log.info("GET /api/ues/semestre/{}", semestre);
        List<UEDTO> ues = ueService.getUEsBySemestre(semestre);
        return ResponseEntity.ok(ues);
    }

    /**
     * Récupère les UEs par niveau
     */
    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<UEDTO>> getUEsByNiveau(@PathVariable String niveau) {
        log.info("GET /api/ues/niveau/{}", niveau);
        List<UEDTO> ues = ueService.getUEsByNiveau(niveau);
        return ResponseEntity.ok(ues);
    }

    /**
     * Récupère les UEs d'un enseignant
     */
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<UEDTO>> getUEsByEnseignant(@PathVariable String enseignantId) {
        log.info("GET /api/ues/enseignant/{}", enseignantId);
        List<UEDTO> ues = ueService.getUEsByEnseignant(enseignantId);
        return ResponseEntity.ok(ues);
    }

    /**
     * Assigne un enseignant à une UE
     */
    @PostMapping("/{ueId}/enseignants/{enseignantId}")
    public ResponseEntity<UEDTO> assignEnseignantToUE(
            @PathVariable Long ueId, 
            @PathVariable String enseignantId) {
        log.info("POST /api/ues/{}/enseignants/{} - Assignation d'enseignant", ueId, enseignantId);
        UEDTO updatedUE = ueService.assignEnseignantToUE(ueId, enseignantId);
        return ResponseEntity.ok(updatedUE);
    }

    /**
     * Assigne un enseignant à une UE (version simplifiée)
     */
    @PostMapping("/{ueId}/enseignants/{enseignantId}/simple")
    public ResponseEntity<UEDTO> assignEnseignantToUESimple(
            @PathVariable Long ueId, 
            @PathVariable String enseignantId) {
        log.info("POST /api/ues/{}/enseignants/{}/simple - Assignation simple d'enseignant", ueId, enseignantId);
        UEDTO updatedUE = ueService.assignEnseignantToUESimple(ueId, enseignantId);
        return ResponseEntity.ok(updatedUE);
    }

    /**
     * Retire un enseignant d'une UE
     */
    @DeleteMapping("/{ueId}/enseignants/{enseignantId}")
    public ResponseEntity<UEDTO> removeEnseignantFromUE(
            @PathVariable Long ueId, 
            @PathVariable String enseignantId) {
        log.info("DELETE /api/ues/{}/enseignants/{} - Retrait d'enseignant", ueId, enseignantId);
        UEDTO updatedUE = ueService.removeEnseignantFromUE(ueId, enseignantId);
        return ResponseEntity.ok(updatedUE);
    }

    /**
     * Récupère les statistiques des UEs
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUEStatistics() {
        log.info("GET /api/ues/statistics - Récupération des statistiques");
        Map<String, Object> statistics = ueService.getUEStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Endpoint de santé pour vérifier le service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("UE Service is running");
    }

    /**
     * Récupère les options pour les formulaires (semestres, niveaux, etc.)
     */
    @GetMapping("/options")
    public ResponseEntity<Map<String, List<String>>> getUEOptions() {
        log.info("GET /api/ues/options - Récupération des options");
        
        Map<String, List<String>> options = Map.of(
            "semestres", List.of("S1", "S2", "S3", "S4", "S5", "S6", "S7", "S8", "S9", "S10"),
            "niveaux", List.of("L1", "L2", "L3", "M1", "M2"),
            "typesCours", List.of("CM", "TD", "TP", "Projet", "Stage")
        );
        
        return ResponseEntity.ok(options);
    }
}