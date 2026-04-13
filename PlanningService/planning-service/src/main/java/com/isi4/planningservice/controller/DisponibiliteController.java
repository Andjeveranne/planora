package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.DisponibiliteDTO;
import com.isi4.planningservice.service.DisponibiliteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planning/disponibilites")
@RequiredArgsConstructor
@Slf4j
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;

    @PostMapping
    public ResponseEntity<?> addDisponibilite(@RequestBody DisponibiliteDTO dto) {
        try {
            log.info("POST /disponibilites - enseignantId: {}", dto.getEnseignantId());
            DisponibiliteDTO saved = disponibiliteService.addDisponibilite(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An unexpected error occurred"));
        }
    }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<DisponibiliteDTO>> getByEnseignant(@PathVariable String enseignantId) {
        log.info("GET /disponibilites/enseignant/{}", enseignantId);
        List<DisponibiliteDTO> disponibilites = disponibiliteService.getDisponibilitesByEnseignant(enseignantId);
        return ResponseEntity.ok(disponibilites);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDisponibilite(@PathVariable Long id) {
        try {
            log.info("DELETE /disponibilites/{}", id);
            disponibiliteService.deleteDisponibilite(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/enseignant/{enseignantId}")
    public ResponseEntity<Void> deleteAllByEnseignant(@PathVariable String enseignantId) {
        log.info("DELETE /disponibilites/enseignant/{}", enseignantId);
        disponibiliteService.deleteAllByEnseignant(enseignantId);
        return ResponseEntity.noContent().build();
    }
}
