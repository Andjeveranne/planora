package com.isi4.userservice.controller;

import com.isi4.userservice.dto.DisponibiliteDTO;
import com.isi4.userservice.service.DisponibiliteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * Contrôleur REST pour la gestion des disponibilités des enseignants
 */
@RestController
@RequestMapping("/api/disponibilites")
@RequiredArgsConstructor
@Slf4j
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;

    /**
     * Crée une nouvelle disponibilité pour un enseignant
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isCurrentUser(#disponibiliteDTO.enseignantId))")
    public ResponseEntity<DisponibiliteDTO> creerDisponibilite(
            @Valid @RequestBody DisponibiliteDTO disponibiliteDTO) {
        log.info("Demande de création de disponibilité pour l'enseignant ID: {}", disponibiliteDTO.getEnseignantId());
        
        DisponibiliteDTO nouvelleDisponibilite = disponibiliteService.creerDisponibilite(disponibiliteDTO);
        return new ResponseEntity<>(nouvelleDisponibilite, HttpStatus.CREATED);
    }

    /**
     * Met à jour une disponibilité existante
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isOwnerOfDisponibilite(#id))")
    public ResponseEntity<DisponibiliteDTO> modifierDisponibilite(
            @PathVariable Long id,
            @Valid @RequestBody DisponibiliteDTO disponibiliteDTO) {
        log.info("Demande de modification de la disponibilité ID: {}", id);
        
        DisponibiliteDTO disponibiliteModifiee = disponibiliteService.modifierDisponibilite(id, disponibiliteDTO);
        return ResponseEntity.ok(disponibiliteModifiee);
    }

    /**
     * Supprime une disponibilité
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isOwnerOfDisponibilite(#id))")
    public ResponseEntity<Void> supprimerDisponibilite(
            @PathVariable Long id) {
        log.info("Demande de suppression de la disponibilité ID: {}", id);
        
        disponibiliteService.supprimerDisponibilite(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupère toutes les disponibilités d'un enseignant
     */
    @GetMapping("/enseignant/{enseignantId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isCurrentUser(#enseignantId))")
    public ResponseEntity<List<DisponibiliteDTO>> getDisponibilitesEnseignant(
            @PathVariable String enseignantId) {
        log.info("Demande de récupération des disponibilités pour l'enseignant ID: {}", enseignantId);
        
        List<DisponibiliteDTO> disponibilites = disponibiliteService.getDisponibilitesEnseignant(enseignantId);
        return ResponseEntity.ok(disponibilites);
    }

    /**
     * Récupère les disponibilités d'un enseignant pour un jour donné
     */
    @GetMapping("/enseignant/{enseignantId}/jour/{jourSemaine}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isCurrentUser(#enseignantId))")
    public ResponseEntity<List<DisponibiliteDTO>> getDisponibilitesEnseignantParJour(
            @PathVariable String enseignantId,
            @PathVariable DayOfWeek jourSemaine) {
        log.info("Demande de récupération des disponibilités pour l'enseignant ID: {} le {}", enseignantId, jourSemaine);
        
        List<DisponibiliteDTO> disponibilites = disponibiliteService.getDisponibilitesEnseignantParJour(enseignantId, jourSemaine);
        return ResponseEntity.ok(disponibilites);
    }

    /**
     * Trouve les enseignants disponibles à un créneau donné
     */
    @GetMapping("/recherche")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getEnseignantsDisponibles(
            @RequestParam DayOfWeek jourSemaine,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime heureDebut,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime heureFin) {
        log.info("Recherche des enseignants disponibles le {} de {} à {}", jourSemaine, heureDebut, heureFin);
        
        List<String> enseignantsDisponibles = disponibiliteService.getEnseignantsDisponibles(jourSemaine, heureDebut, heureFin);
        return ResponseEntity.ok(enseignantsDisponibles);
    }

    /**
     * Supprime toutes les disponibilités d'un enseignant
     */
    @DeleteMapping("/enseignant/{enseignantId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ENSEIGNANT') and @securityService.isCurrentUser(#enseignantId))")
    public ResponseEntity<Void> supprimerToutesDisponibilitesEnseignant(
            @PathVariable String enseignantId) {
        log.info("Demande de suppression de toutes les disponibilités pour l'enseignant ID: {}", enseignantId);
        
        disponibiliteService.supprimerToutesDisponibilitesEnseignant(enseignantId);
        return ResponseEntity.noContent().build();
    }
}