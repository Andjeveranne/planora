package com.isi4.classesservice.controller;

import com.isi4.classesservice.dto.SpecialiteDTO;
import com.isi4.classesservice.service.SpecialiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    @GetMapping
    public ResponseEntity<List<SpecialiteDTO>> obtenirToutes() {
        return ResponseEntity.ok(specialiteService.obtenirToutes());
    }

    @GetMapping("/departement/{idFiliere}")
    public ResponseEntity<List<SpecialiteDTO>> obtenirParDepartement(@PathVariable String idFiliere) {
        return ResponseEntity.ok(specialiteService.obtenirParDepartement(idFiliere));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteDTO> creer(@Valid @RequestBody SpecialiteDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialiteService.creer(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SpecialiteDTO> mettreAJour(
            @PathVariable String id, @Valid @RequestBody SpecialiteDTO dto) {
        return ResponseEntity.ok(specialiteService.mettreAJour(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable String id) {
        specialiteService.supprimer(id);
        return ResponseEntity.ok(Map.of("message", "Spécialité supprimée avec succès"));
    }
}
