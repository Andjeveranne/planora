package com.isi4.userservice.controller;

import com.isi4.userservice.dto.EtudiantDTO;
import com.isi4.userservice.dto.EtudiantResponseDTO;
import com.isi4.userservice.service.EtudiantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtudiantResponseDTO> createEtudiant(@Valid @RequestBody EtudiantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(etudiantService.createEtudiant(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT', 'ETUDIANT')")
    public ResponseEntity<List<EtudiantResponseDTO>> getAllEtudiants() {
        return ResponseEntity.ok(etudiantService.getAllEtudiants());
    }

    @GetMapping("/ecole/{idEcole}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT', 'ETUDIANT')")
    public ResponseEntity<List<EtudiantResponseDTO>> getEtudiantsByEcole(@PathVariable String idEcole) {
        return ResponseEntity.ok(etudiantService.getEtudiantsByEcole(idEcole));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT', 'ETUDIANT')")
    public ResponseEntity<EtudiantResponseDTO> getEtudiantById(@PathVariable String id) {
        return ResponseEntity.ok(etudiantService.getEtudiantById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EtudiantResponseDTO> updateEtudiant(@PathVariable String id, @Valid @RequestBody EtudiantDTO dto) {
        return ResponseEntity.ok(etudiantService.updateEtudiant(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEtudiant(@PathVariable String id) {
        etudiantService.deleteEtudiant(id);
        return ResponseEntity.noContent().build();
    }
}
