package com.isi4.notificationservice.controller;

import com.isi4.notificationservice.dto.NotificationDTO;
import com.isi4.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationDTO> envoyer(@Valid @RequestBody NotificationDTO dto) {
        log.info("POST /api/notifications - destinataire={}", dto.getDestinataireId());
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.envoyer(dto));
        } catch (Exception e) {
            log.error("Erreur envoi notification", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/utilisateur/{destinataireId}")
    public ResponseEntity<List<NotificationDTO>> getByDestinataire(@PathVariable String destinataireId) {
        log.info("GET /api/notifications/utilisateur/{}", destinataireId);
        return ResponseEntity.ok(notificationService.getByDestinataire(destinataireId));
    }

    @GetMapping("/utilisateur/{destinataireId}/non-lues")
    public ResponseEntity<List<NotificationDTO>> getNonLues(@PathVariable String destinataireId) {
        log.info("GET /api/notifications/utilisateur/{}/non-lues", destinataireId);
        return ResponseEntity.ok(notificationService.getNonLues(destinataireId));
    }

    @GetMapping("/utilisateur/{destinataireId}/count")
    public ResponseEntity<Map<String, Long>> countNonLues(@PathVariable String destinataireId) {
        long count = notificationService.countNonLues(destinataireId);
        return ResponseEntity.ok(Map.of("nonLues", count));
    }

    @PutMapping("/{id}/lire")
    public ResponseEntity<NotificationDTO> marquerCommeLu(@PathVariable Long id) {
        log.info("PUT /api/notifications/{}/lire", id);
        try {
            return ResponseEntity.ok(notificationService.marquerCommeLu(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/utilisateur/{destinataireId}/lire-tout")
    public ResponseEntity<Void> marquerToutesCommeLues(@PathVariable String destinataireId) {
        log.info("PUT /api/notifications/utilisateur/{}/lire-tout", destinataireId);
        notificationService.marquerToutesCommeLues(destinataireId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        log.info("DELETE /api/notifications/{}", id);
        try {
            notificationService.supprimer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
