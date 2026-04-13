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

@RestController
@RequestMapping("/api/ues")
@RequiredArgsConstructor
public class UEController {

    private final UEService ueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<UEDTO> creer(@Valid @RequestBody UEDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ueService.creer(dto));
    }

    @GetMapping
    public ResponseEntity<List<UEDTO>> obtenirToutes() {
        return ResponseEntity.ok(ueService.obtenirToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UEDTO> obtenirParId(@PathVariable Long id) {
        return ResponseEntity.ok(ueService.obtenirParId(id));
    }

    @GetMapping("/filiere/{idFiliere}")
    public ResponseEntity<List<UEDTO>> obtenirParFiliere(@PathVariable String idFiliere) {
        return ResponseEntity.ok(ueService.obtenirParFiliere(idFiliere));
    }

    @GetMapping("/niveau/{niveau}")
    public ResponseEntity<List<UEDTO>> obtenirParNiveau(@PathVariable String niveau) {
        return ResponseEntity.ok(ueService.obtenirParNiveau(niveau));
    }

    @GetMapping("/filiere/{idFiliere}/niveau/{niveau}")
    public ResponseEntity<List<UEDTO>> obtenirParFiliereEtNiveau(
            @PathVariable String idFiliere,
            @PathVariable String niveau) {
        return ResponseEntity.ok(ueService.obtenirParFiliereEtNiveau(idFiliere, niveau));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<UEDTO> mettreAJour(@PathVariable Long id, @Valid @RequestBody UEDTO dto) {
        return ResponseEntity.ok(ueService.mettreAJour(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable Long id) {
        ueService.supprimer(id);
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
