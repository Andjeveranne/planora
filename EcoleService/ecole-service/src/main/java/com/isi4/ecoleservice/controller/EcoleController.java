package com.isi4.ecoleservice.controller;

import com.isi4.ecoleservice.dto.EcoleDTO;
import com.isi4.ecoleservice.service.EcoleService;
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
 * Contrôleur REST CRUD pour les écoles (conforme au diagramme de classes).
 */
@RestController
@RequestMapping("/api/ecoles")
@RequiredArgsConstructor
public class EcoleController {

    private final EcoleService ecoleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<EcoleDTO> creer(@Valid @RequestBody EcoleDTO dto) {
        EcoleDTO created = ecoleService.creer(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<EcoleDTO>> obtenirToutes() {
        return ResponseEntity.ok(ecoleService.obtenirToutes());
    }

    @GetMapping("/{idEcole}")
    public ResponseEntity<EcoleDTO> obtenirParId(@PathVariable String idEcole) {
        return ResponseEntity.ok(ecoleService.obtenirParId(idEcole));
    }

    @PutMapping("/{idEcole}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<EcoleDTO> mettreAJour(
            @PathVariable String idEcole,
            @Valid @RequestBody EcoleDTO dto) {
        return ResponseEntity.ok(ecoleService.mettreAJour(idEcole, dto));
    }

    @DeleteMapping("/{idEcole}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable String idEcole) {
        ecoleService.supprimer(idEcole);
        Map<String, String> response = new HashMap<>();
        response.put("message", "École supprimée avec succès");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ecole-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
