package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.DisponibiliteDTO;
import com.isi4.planningservice.model.Disponibilite;
import com.isi4.planningservice.service.DisponibiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planning/disponibilites")
@RequiredArgsConstructor
public class DisponibiliteController {

    private final DisponibiliteService disponibiliteService;

    @PostMapping
    public ResponseEntity<Disponibilite> addDisponibilite(@RequestBody DisponibiliteDTO dto) {
        Disponibilite dispo = disponibiliteService.addDisponibilite(
                dto.getEnseignantId(),
                dto.getDateHeureDebut(),
                dto.getDateHeureFin(),
                dto.getCommentaire());
        return ResponseEntity.status(HttpStatus.CREATED).body(dispo);
    }

    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<Disponibilite>> getByEnseignant(@PathVariable String enseignantId) {
        List<Disponibilite> dispos = disponibiliteService.getDisponibilitesByEnseignant(enseignantId);
        return ResponseEntity.ok(dispos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisponibilite(@PathVariable Long id) {
        disponibiliteService.deleteDisponibilite(id);
        return ResponseEntity.noContent().build();
    }
}
