package com.isi4.userservice.controller;

import com.isi4.userservice.dto.EnseignantUEDTO;
import com.isi4.userservice.service.EnseignantUEService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enseignant-ues")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnseignantUEController {

    private final EnseignantUEService enseignantUEService;

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<EnseignantUEDTO>> getUEsByEnseignant(@PathVariable String enseignantId) {
        List<EnseignantUEDTO> ues = enseignantUEService.getUEsByEnseignant(enseignantId);
        return ResponseEntity.ok(ues);
    }

    @GetMapping("/ue/{ueId}")
    public ResponseEntity<List<EnseignantUEDTO>> getEnseignantsByUE(@PathVariable Long ueId) {
        List<EnseignantUEDTO> enseignants = enseignantUEService.getEnseignantsByUE(ueId);
        return ResponseEntity.ok(enseignants);
    }

    @GetMapping("/ue/{ueId}/type/{typeCours}")
    public ResponseEntity<List<EnseignantUEDTO>> getEnseignantsDisponiblesPourUE(
            @PathVariable Long ueId, 
            @PathVariable String typeCours) {
        List<EnseignantUEDTO> enseignants = enseignantUEService.getEnseignantsDisponiblesPourUE(ueId, typeCours);
        return ResponseEntity.ok(enseignants);
    }

    @PostMapping
    public ResponseEntity<EnseignantUEDTO> ajouterCompetence(@RequestBody EnseignantUEDTO dto) {
        EnseignantUEDTO result = enseignantUEService.ajouterCompetence(dto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerCompetence(@PathVariable Long id) {
        enseignantUEService.supprimerCompetence(id);
        return ResponseEntity.ok().build();
    }
}