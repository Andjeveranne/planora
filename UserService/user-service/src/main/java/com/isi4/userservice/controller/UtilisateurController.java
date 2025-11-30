package com.isi4.userservice.controller;

import com.isi4.userservice.dto.EnseignantDTO;
import com.isi4.userservice.dto.UtilisateurDTO;
import com.isi4.userservice.dto.UtilisateurResponseDTO;
import com.isi4.userservice.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {
    
    private final UtilisateurService utilisateurService;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creerUtilisateur(@Valid @RequestBody UtilisateurDTO dto) {
        UtilisateurResponseDTO response = utilisateurService.creerUtilisateur(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/enseignant")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> creerEnseignant(@Valid @RequestBody EnseignantDTO dto) {
        dto.setRole("ENSEIGNANT");
        UtilisateurResponseDTO response = utilisateurService.creerUtilisateur(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUtilisateurs() {
        List<UtilisateurResponseDTO> utilisateurs = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(utilisateurs);
    }
    
    @GetMapping("/enseignants")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENSEIGNANT')")
    public ResponseEntity<List<UtilisateurResponseDTO>> getEnseignants() {
        List<UtilisateurResponseDTO> enseignants = utilisateurService.getEnseignants();
        return ResponseEntity.ok(enseignants);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> getUtilisateurById(@PathVariable String id) {
        UtilisateurResponseDTO utilisateur = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(utilisateur);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDTO> updateUtilisateur(
            @PathVariable String id,
            @Valid @RequestBody UtilisateurDTO dto) {
        UtilisateurResponseDTO response = utilisateurService.updateUtilisateur(id, dto);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable String id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }
}
