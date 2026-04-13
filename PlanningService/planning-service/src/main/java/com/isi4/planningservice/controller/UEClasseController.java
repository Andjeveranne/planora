package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.UEClasseDTO;
import com.isi4.planningservice.service.UEClasseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning/ue-classes")
@RequiredArgsConstructor
@Slf4j
public class UEClasseController {

    private final UEClasseService ueClasseService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UEClasseDTO> creer(@RequestBody UEClasseDTO dto) {
        log.info("Création d'une relation UE-Classe: UE={}, Classe={}", dto.getUeId(), dto.getClasseId());
        
        try {
            UEClasseDTO created = ueClasseService.creer(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            log.error("Erreur de validation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UEClasseDTO>> obtenirTous() {
        log.info("Récupération de toutes les relations UE-Classe");
        List<UEClasseDTO> ueClasses = ueClasseService.obtenirTous();
        return ResponseEntity.ok(ueClasses);
    }

    @GetMapping("/ue/{ueId}")
    public ResponseEntity<List<UEClasseDTO>> obtenirParUE(@PathVariable Long ueId) {
        log.info("Récupération des classes pour l'UE {}", ueId);
        List<UEClasseDTO> ueClasses = ueClasseService.obtenirParUE(ueId);
        return ResponseEntity.ok(ueClasses);
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<UEClasseDTO>> obtenirParClasse(@PathVariable String classeId) {
        log.info("Récupération des UEs pour la classe {}", classeId);
        List<UEClasseDTO> ueClasses = ueClasseService.obtenirParClasse(classeId);
        return ResponseEntity.ok(ueClasses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UEClasseDTO> mettreAJour(@PathVariable Long id, @RequestBody UEClasseDTO dto) {
        log.info("Mise à jour de la relation UE-Classe {}", id);
        
        try {
            UEClasseDTO updated = ueClasseService.mettreAJour(id, dto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("Erreur: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        log.info("Suppression de la relation UE-Classe {}", id);
        
        try {
            ueClasseService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Erreur: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}