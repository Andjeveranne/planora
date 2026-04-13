package com.isi4.classesservice.controller;

import com.isi4.classesservice.dto.FiliereDTO;
import com.isi4.classesservice.service.FiliereService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FiliereDTO> creer(@Valid @RequestBody FiliereDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filiereService.creer(dto));
    }

    @GetMapping
    public ResponseEntity<List<FiliereDTO>> obtenirToutes() {
        return ResponseEntity.ok(filiereService.obtenirToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiliereDTO> obtenirParId(@PathVariable String id) {
        return ResponseEntity.ok(filiereService.obtenirParId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FiliereDTO> mettreAJour(@PathVariable String id, @Valid @RequestBody FiliereDTO dto) {
        return ResponseEntity.ok(filiereService.mettreAJour(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable String id) {
        filiereService.supprimer(id);
        return ResponseEntity.ok(Map.of("message", "Filière supprimée avec succès"));
    }
}
