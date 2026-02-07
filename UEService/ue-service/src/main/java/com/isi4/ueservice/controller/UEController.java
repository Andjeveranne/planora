package com.isi4.ueservice.controller;

import com.isi4.ueservice.dto.UEDTO;
import com.isi4.ueservice.service.UEService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST CRUD pour les Unités d'enseignement (UE), conforme au diagramme de classes.
 */
@RestController
@RequestMapping("/api/ues")
@RequiredArgsConstructor
public class UEController {

    private final UEService ueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<UEDTO> creer(@Valid @RequestBody UEDTO dto) {
        UEDTO created = ueService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<UEDTO>> obtenirToutes() {
        return ResponseEntity.ok(ueService.obtenirToutes());
    }

    @GetMapping("/{idUE}")
    public ResponseEntity<UEDTO> obtenirParId(@PathVariable String idUE) {
        return ResponseEntity.ok(ueService.obtenirParId(idUE));
    }

    @PutMapping("/{idUE}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<UEDTO> mettreAJour(
            @PathVariable String idUE,
            @Valid @RequestBody UEDTO dto) {
        return ResponseEntity.ok(ueService.mettreAJour(idUE, dto));
    }

    @DeleteMapping("/{idUE}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable String idUE) {
        ueService.supprimer(idUE);
        Map<String, String> response = new HashMap<>();
        response.put("message", "UE supprimée avec succès");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ue-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
