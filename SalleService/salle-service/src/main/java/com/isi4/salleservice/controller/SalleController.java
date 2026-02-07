package com.isi4.salleservice.controller;

import com.isi4.salleservice.dto.SalleDTO;
import com.isi4.salleservice.enums.StatutSalle;
import com.isi4.salleservice.enums.TypeSalle;
import com.isi4.salleservice.service.SalleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salles")
@RequiredArgsConstructor
public class SalleController {
    
    private final SalleService salleService;
    
    // Créer une nouvelle salle - ADMIN SEULEMENT
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<SalleDTO> creerSalle(@Valid @RequestBody SalleDTO salleDTO) {
        SalleDTO nouvelleSalle = salleService.creerSalle(salleDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nouvelleSalle);
    }
    
    // Obtenir toutes les salles - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping
    public ResponseEntity<List<SalleDTO>> obtenirToutesLesSalles() {
        List<SalleDTO> salles = salleService.obtenirToutesLesSalles();
        return ResponseEntity.ok(salles);
    }
    
    // Obtenir une salle par ID - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping("/{idSalle}")
    public ResponseEntity<SalleDTO> obtenirSalleParId(@PathVariable String idSalle) {
        SalleDTO salle = salleService.obtenirSalleParId(idSalle);
        return ResponseEntity.ok(salle);
    }
    
    // Obtenir les salles par école - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping("/ecole/{idEcole}")
    public ResponseEntity<List<SalleDTO>> obtenirSallesParEcole(@PathVariable String idEcole) {
        List<SalleDTO> salles = salleService.obtenirSallesParEcole(idEcole);
        return ResponseEntity.ok(salles);
    }
    
    // Obtenir les salles par type - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SalleDTO>> obtenirSallesParType(@PathVariable TypeSalle type) {
        List<SalleDTO> salles = salleService.obtenirSallesParType(type);
        return ResponseEntity.ok(salles);
    }
    
    // Obtenir les salles par statut - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<SalleDTO>> obtenirSallesParStatut(@PathVariable StatutSalle statut) {
        List<SalleDTO> salles = salleService.obtenirSallesParStatut(statut);
        return ResponseEntity.ok(salles);
    }
    
    // Obtenir les salles disponibles avec capacité minimale - TOUS LES UTILISATEURS CONNECTÉS
    @GetMapping("/disponibles")
    public ResponseEntity<List<SalleDTO>> obtenirSallesDisponibles(
            @RequestParam String idEcole,
            @RequestParam(defaultValue = "1") Integer capaciteMin) {
        List<SalleDTO> salles = salleService.obtenirSallesDisponibles(idEcole, capaciteMin);
        return ResponseEntity.ok(salles);
    }
    
    // Mettre à jour une salle - ADMIN SEULEMENT
    @PutMapping("/{idSalle}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<SalleDTO> mettreAJourSalle(
            @PathVariable String idSalle,
            @Valid @RequestBody SalleDTO salleDTO) {
        SalleDTO salleMiseAJour = salleService.mettreAJourSalle(idSalle, salleDTO);
        return ResponseEntity.ok(salleMiseAJour);
    }
    
    // Changer le statut d'une salle - ADMIN SEULEMENT
    @PatchMapping("/{idSalle}/statut")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<SalleDTO> changerStatutSalle(
            @PathVariable String idSalle,
            @RequestParam StatutSalle statut) {
        SalleDTO salleMiseAJour = salleService.changerStatutSalle(idSalle, statut);
        return ResponseEntity.ok(salleMiseAJour);
    }
    
    // Supprimer une salle - ADMIN SEULEMENT
    @DeleteMapping("/{idSalle}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ADMINISTRATEUR')")
    public ResponseEntity<Map<String, String>> supprimerSalle(@PathVariable String idSalle) {
        salleService.supprimerSalle(idSalle);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Salle supprimée avec succès");
        return ResponseEntity.ok(response);
    }
    
    // Endpoint de debug pour tester l'authentification
    @GetMapping("/debug/auth")
    public ResponseEntity<Map<String, Object>> debugAuth(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Headers reçus
        Map<String, String> headers = new HashMap<>();
        request.getHeaderNames().asIterator().forEachRemaining(name -> 
            headers.put(name, request.getHeader(name))
        );
        
        // Informations d'authentification
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        response.put("headers", headers);
        response.put("authenticated", auth != null && auth.isAuthenticated());
        response.put("principal", auth != null ? auth.getPrincipal() : null);
        response.put("authorities", auth != null ? auth.getAuthorities() : null);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    // Endpoint de santé - PUBLIC
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "salle-service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
    
    // Obtenir les types de salles disponibles - PUBLIC
    @GetMapping("/types")
    public ResponseEntity<TypeSalle[]> obtenirTypesSalles() {
        return ResponseEntity.ok(TypeSalle.values());
    }
    
    // Obtenir les statuts disponibles - PUBLIC
    @GetMapping("/statuts")
    public ResponseEntity<StatutSalle[]> obtenirStatutsSalles() {
        return ResponseEntity.ok(StatutSalle.values());
    }
}