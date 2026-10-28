package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.dto.GenerationRequestDto;
import com.isi4.planningservice.dto.GenerationSummaryDto;
import com.isi4.planningservice.service.CanonicalTimetableGenerationService;
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
import java.util.HashMap;

@RestController
@RequestMapping("/api/planning/emplois-du-temps")
@RequiredArgsConstructor
@Slf4j
public class EmploiDuTempsController {

    private final EmploiDuTempsService emploiDuTempsService;
    private final EmploiDuTempsServiceV2 emploiDuTempsServiceV2;
    private final CanonicalTimetableGenerationService canonicalTimetableGenerationService;

    @PostMapping("/generer-canonique")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenerationSummaryDto> genererEmploisDuTempsCanonique(@RequestBody GenerationRequestDto request) {
        log.info("Génération canonique EDT: school={}, department={}, speciality={}, level={}",
                request.getSchoolId(), request.getDepartmentId(), request.getSpecialityId(), request.getLevelId());
        GenerationSummaryDto summary = canonicalTimetableGenerationService.generate(request);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/generer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> genererEmploisDuTemps(
            @RequestParam(required = false) String idEcole,
            @RequestParam(required = false) String departement,
            @RequestParam(required = false) String specialite) {
        log.info("Génération EDT école={}, département={}, spécialité={}", idEcole, departement, specialite);
        try {
            List<EmploiDuTempsDTO> emplois = emploiDuTempsServiceV2.genererEmploisDuTemps(idEcole, departement, specialite);
            log.info("Génération réussie: {} créneaux planifiés", emplois.size());
            return ResponseEntity.ok(emplois);
        } catch (IllegalStateException e) {
            log.error("Erreur métier lors de la génération", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur génération", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/generer-v2")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> genererEmploisDuTempsV2(@RequestBody(required = false) Map<String, String> request) {
        String idEcole = request == null ? null : request.get("idEcole");
        String departement = request == null ? null : request.get("departement");
        String specialite = request == null ? null : request.get("specialite");
        log.info("Génération V2 des emplois du temps pour l'école {}", idEcole);
        try {
            List<EmploiDuTempsDTO> emplois = emploiDuTempsServiceV2.genererEmploisDuTemps(idEcole, departement, specialite);
            log.info("Génération V2 réussie: {} créneaux planifiés", emplois.size());
            return ResponseEntity.ok(emplois);
        } catch (Exception e) {
            log.error("Erreur lors de la génération V2 des emplois du temps", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/enseignants-disponibles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getEnseignantsAvecDisponibilites(
            @RequestParam(required = false) String idEcole,
            @RequestParam(required = false) String departement,
            @RequestParam(required = false) String specialite) {
        try {
            List<Map<String, Object>> enseignants = emploiDuTempsServiceV2.getEnseignantsAvecDisponibilitesDetailles(idEcole, departement, specialite);
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