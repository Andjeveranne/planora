package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.UEEnseignantDTO;
import com.isi4.planningservice.service.UEEnseignantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning/ue-enseignants")
@RequiredArgsConstructor
@Slf4j
public class UEEnseignantController {

    private final UEEnseignantService ueEnseignantService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<UEEnseignantDTO> creer(@RequestBody UEEnseignantDTO dto) {
        log.info("Création d'une relation UE-Enseignant: UE={}, Enseignant={}", dto.getUeId(), dto.getEnseignantId());
        
        try {
            UEEnseignantDTO created = ueEnseignantService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UEEnseignantDTO>> obtenirTous() {
        log.info("Récupération de toutes les relations UE-Enseignant");
        List<UEEnseignantDTO> ueEnseignants = ueEnseignantService.obtenirTous();
        return ResponseEntity.ok(ueEnseignants);
    }

    @GetMapping("/ue/{ueId}")
    public ResponseEntity<List<UEEnseignantDTO>> obtenirParUE(@PathVariable Long ueId) {
        log.info("Récupération des enseignants pour l'UE {}", ueId);
        List<UEEnseignantDTO> ueEnseignants = ueEnseignantService.obtenirParUE(ueId);
        return ResponseEntity.ok(ueEnseignants);
    }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<UEEnseignantDTO>> obtenirParEnseignant(@PathVariable String enseignantId) {
        log.info("Récupération des UEs pour l'enseignant {}", enseignantId);
        List<UEEnseignantDTO> ueEnseignants = ueEnseignantService.obtenirParEnseignant(enseignantId);
        return ResponseEntity.ok(ueEnseignants);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<UEEnseignantDTO> mettreAJour(@PathVariable Long id, @RequestBody UEEnseignantDTO dto) {
        log.info("Mise à jour de la relation UE-Enseignant {}", id);
        
        try {
            UEEnseignantDTO updated = ueEnseignantService.mettreAJour(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Erreur: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        log.info("Suppression de la relation UE-Enseignant {}", id);
        
        try {
            ueEnseignantService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}