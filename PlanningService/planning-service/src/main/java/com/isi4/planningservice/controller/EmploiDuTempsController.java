package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.service.EmploiDuTempsService;
import com.isi4.planningservice.service.EmploiDuTempsServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planning/emplois-du-temps")
@RequiredArgsConstructor
@Slf4j
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final EmploiDuTempsServiceV2 emploiDuTempsServiceV2;

    @PostMapping("/generer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmploiDuTempsDTO>> genererEmploisDuTemps(
            @RequestParam(required = false) String idEcole) {
        log.info("Génération des emplois du temps (semaine à venir) pour l'école {}", idEcole);
        try {
            List<EmploiDuTempsDTO> emplois = emploiDuTempsService.genererEmploisDuTemps(1, idEcole);
            log.info("Génération réussie: {} créneaux planifiés", emplois.size());
            return ResponseEntity.ok(emplois);
        } catch (Exception e) {
            log.error("Erreur génération", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/generer-v2")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmploiDuTempsDTO>> genererEmploisDuTempsV2(@RequestBody Map<String, String> request) {
        String idEcole = request.get("idEcole");
        if (idEcole == null || idEcole.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Génération V2 des emplois du temps pour l'école {}", idEcole);
        try {
            List<EmploiDuTempsDTO> emplois = emploiDuTempsServiceV2.genererEmploisDuTemps(idEcole);
            log.info("Génération V2 réussie: {} créneaux planifiés", emplois.size());
            return ResponseEntity.ok(emplois);
        } catch (Exception e) {
            log.error("Erreur lors de la génération V2 des emplois du temps", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/enseignants-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getEnseignantsAvecDisponibilites() {
        try {
            List<Map<String, Object>> enseignants = emploiDuTempsServiceV2.getEnseignantsAvecDisponibilitesDetailles();
            return ResponseEntity.ok(enseignants);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des enseignants avec disponibilités", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploiDuTempsClasse(
            @PathVariable String classeId,
            @RequestParam(required = false) Integer semaine) {
        
        log.info("Récupération de l'emploi du temps pour la classe {} (semaine: {})", classeId, semaine);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploiDuTempsClasse(classeId, semaine);
        return ResponseEntity.ok(emplois);
    }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<EmploiDuTempsDTO>> getEmploiDuTempsEnseignant(
            @PathVariable String enseignantId,
            @RequestParam(required = false) Integer semaine) {
        
        log.info("Récupération de l'emploi du temps pour l'enseignant {} (semaine: {})", enseignantId, semaine);
        
        List<EmploiDuTempsDTO> emplois = emploiDuTempsService.getEmploiDuTempsEnseignant(enseignantId, semaine);
        return ResponseEntity.ok(emplois);
    }

    @DeleteMapping("/supprimer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimerEmploisDuTemps(
            @RequestParam Integer semaine,
            @RequestParam String idEcole) {
        
        log.info("Suppression des emplois du temps pour la semaine {} et l'école {}", semaine, idEcole);
        
        emploiDuTempsService.supprimerEmploisDuTemps(semaine, idEcole);
        return ResponseEntity.noContent().build();
    }
}