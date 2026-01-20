package com.isi4.userservice.controller;

import com.isi4.userservice.dto.EmploiDuTempsDTO;
import com.isi4.userservice.service.EmploiDuTempsService;
import com.isi4.userservice.service.TimetableGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des emplois du temps
 */
@RestController
@RequestMapping("/api/emplois-du-temps")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final TimetableGenerationService timetableGenerationService;

    /**
     * Récupère tous les emplois du temps d'un enseignant
     */
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisDuTempsByEnseignant(
            @PathVariable String enseignantId) {
        log.info("GET /api/emplois-du-temps/enseignant/{}", enseignantId);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploisDuTempsByEnseignant(enseignantId);
        return ResponseEntity.ok(emplois);
    }

    /**
     * Récupère les emplois du temps d'un enseignant pour une période donnée
     */
    @GetMapping("/enseignant/{enseignantId}/periode")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisDuTempsByEnseignantAndPeriode(
            @PathVariable String enseignantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        log.info("GET /api/emplois-du-temps/enseignant/{}/periode?dateDebut={}&dateFin={}", 
                enseignantId, dateDebut, dateFin);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploisDuTempsByEnseignantAndPeriode(
                enseignantId, dateDebut, dateFin);
        return ResponseEntity.ok(emplois);
    }

    /**
     * Récupère les emplois du temps par salle
     */
    @GetMapping("/salle/{salle}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisDuTempsBySalle(@PathVariable String salle) {
        log.info("GET /api/emplois-du-temps/salle/{}", salle);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploisDuTempsBySalle(salle);
        return ResponseEntity.ok(emplois);
    }

    /**
     * Récupère les emplois du temps par classe
     */
    @GetMapping("/classe/{classe}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploisDuTempsByClasse(@PathVariable String classe) {
        log.info("GET /api/emplois-du-temps/classe/{}", classe);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploisDuTempsByClasse(classe);
        return ResponseEntity.ok(emplois);
    }

    /**
     * Crée un nouvel emploi du temps
     */
    @PostMapping
    public ResponseEntity<EmploiDuTempsDTO> createEmploiDuTemps(@Valid @RequestBody EmploiDuTempsDTO emploiDTO) {
        log.info("POST /api/emplois-du-temps - Création d'un emploi du temps pour l'enseignant: {}", 
                emploiDTO.getEnseignantId());
        
        EmploiDuTempsDTO createdEmploi = emploiDuTempsService.createEmploiDuTemps(emploiDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmploi);
    }

    /**
     * Met à jour un emploi du temps existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmploiDuTempsDTO> updateEmploiDuTemps(
            @PathVariable Long id, 
            @Valid @RequestBody EmploiDuTempsDTO emploiDTO) {
        log.info("PUT /api/emplois-du-temps/{} - Mise à jour de l'emploi du temps", id);
        
        EmploiDuTempsDTO updatedEmploi = emploiDuTempsService.updateEmploiDuTemps(id, emploiDTO);
        return ResponseEntity.ok(updatedEmploi);
    }

    /**
     * Supprime un emploi du temps
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmploiDuTemps(@PathVariable Long id) {
        log.info("DELETE /api/emplois-du-temps/{} - Suppression de l'emploi du temps", id);
        
        emploiDuTempsService.deleteEmploiDuTemps(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprime tous les emplois du temps d'un enseignant
     */
    @DeleteMapping("/enseignant/{enseignantId}")
    public ResponseEntity<Void> deleteAllEmploisDuTempsByEnseignant(@PathVariable String enseignantId) {
        log.info("DELETE /api/emplois-du-temps/enseignant/{} - Suppression de tous les emplois du temps", 
                enseignantId);
        
        emploiDuTempsService.deleteAllEmploisDuTempsByEnseignant(enseignantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Endpoint de santé pour vérifier le service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("EmploiDuTemps Service is running");
    }

    /**
     * NOUVEAU: Génère un emploi du temps intelligent basé sur les disponibilités et cours assignés
     */
    @PostMapping("/generer-intelligent/{enseignantId}")
    public ResponseEntity<Map<String, Object>> genererEmploiDuTempsIntelligent(
            @PathVariable String enseignantId,
            @RequestParam(defaultValue = "4") int nombreSemaines) {
        log.info("POST /api/emplois-du-temps/generer-intelligent/{} - Génération intelligente sur {} semaines", 
                enseignantId, nombreSemaines);
        
        try {
            List<EmploiDuTempsDTO> emploisGeneres = timetableGenerationService.genererEmploiDuTempsIntelligent(
                    enseignantId, nombreSemaines);
            
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Emploi du temps généré avec succès",
                "nombreCreneaux", emploisGeneres.size(),
                "emplois", emploisGeneres
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la génération d'emploi du temps pour l'enseignant {}: {}", 
                    enseignantId, e.getMessage(), e);
            
            Map<String, Object> errorResponse = Map.of(
                "success", false,
                "message", "Erreur lors de la génération: " + e.getMessage(),
                "nombreCreneaux", 0,
                "emplois", List.of()
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}