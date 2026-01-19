package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.EmploiDuTempsDTO;
import com.isi4.planningservice.model.EmploiDuTemps;
import com.isi4.planningservice.service.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planning/edt")
@RequiredArgsConstructor
public class EmploiDuTempsController {

    private final EmploiDuTempsService edtService;

    /**
     * Récupérer tout l'EDT d'un enseignant
     */
    @GetMapping("/enseignant/{enseignantId}")
    public ResponseEntity<List<EmploiDuTemps>> getEdtByEnseignant(@PathVariable String enseignantId) {
        List<EmploiDuTemps> edt = edtService.getEdtByEnseignant(enseignantId);
        return ResponseEntity.ok(edt);
    }

    /**
     * Récupérer l'EDT d'un enseignant pour une semaine donnée
     */
    @GetMapping("/enseignant/{enseignantId}/semaine")
    public ResponseEntity<List<EmploiDuTemps>> getEdtByEnseignantAndWeek(
            @PathVariable String enseignantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        List<EmploiDuTemps> edt = edtService.getEdtByEnseignantAndWeek(enseignantId, dateDebut, dateFin);
        return ResponseEntity.ok(edt);
    }

    /**
     * Ajouter un cours manuellement
     */
    @PostMapping
    public ResponseEntity<EmploiDuTemps> addCours(@RequestBody EmploiDuTempsDTO dto) {
        EmploiDuTemps edt = edtService.addCours(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(edt);
    }

    /**
     * Générer automatiquement l'EDT à partir des disponibilités
     */
    @PostMapping("/generer/{enseignantId}")
    public ResponseEntity<List<EmploiDuTemps>> genererEdt(@PathVariable String enseignantId) {
        List<EmploiDuTemps> edt = edtService.genererEdtFromDisponibilites(enseignantId);
        if (edt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(edt);
    }

    /**
     * Supprimer tout l'EDT d'un enseignant
     */
    @DeleteMapping("/enseignant/{enseignantId}")
    public ResponseEntity<Void> deleteEdtByEnseignant(@PathVariable String enseignantId) {
        edtService.deleteEdtByEnseignant(enseignantId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Supprimer un cours spécifique
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCours(@PathVariable Long id) {
        edtService.deleteCours(id);
        return ResponseEntity.noContent().build();
    }
}
