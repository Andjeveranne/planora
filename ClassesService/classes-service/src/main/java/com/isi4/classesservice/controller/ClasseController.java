package com.isi4.classesservice.controller;

import com.isi4.classesservice.dto.ClasseDTO;
import com.isi4.classesservice.service.ClasseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseService classeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClasseDTO> creer(@Valid @RequestBody ClasseDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classeService.creer(dto));
    }

    @GetMapping
    public ResponseEntity<List<ClasseDTO>> obtenirToutes() {
        return ResponseEntity.ok(classeService.obtenirToutes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClasseDTO> obtenirParId(@PathVariable String id) {
        return ResponseEntity.ok(classeService.obtenirParId(id));
    }

    @GetMapping("/filiere/{idFiliere}")
    public ResponseEntity<List<ClasseDTO>> obtenirParFiliere(@PathVariable String idFiliere) {
        return ResponseEntity.ok(classeService.obtenirParFiliere(idFiliere));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ClasseDTO> mettreAJour(@PathVariable String id, @Valid @RequestBody ClasseDTO dto) {
        return ResponseEntity.ok(classeService.mettreAJour(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> supprimer(@PathVariable String id) {
        classeService.supprimer(id);
        return ResponseEntity.ok(Map.of("message", "Classe supprimée avec succès"));
    }
}
