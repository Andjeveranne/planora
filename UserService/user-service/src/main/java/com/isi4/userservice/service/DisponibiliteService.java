package com.isi4.userservice.service;

import com.isi4.userservice.dto.DisponibiliteDTO;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.Disponibilite;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.repository.DisponibiliteRepository;
import com.isi4.userservice.repository.EnseignantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des disponibilités des enseignants
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    private final EnseignantRepository enseignantRepository;

    /**
     * Crée une nouvelle disponibilité pour un enseignant
     */
    public DisponibiliteDTO creerDisponibilite(DisponibiliteDTO disponibiliteDTO) {
        log.info("Création d'une nouvelle disponibilité pour l'enseignant ID: {}", disponibiliteDTO.getEnseignantId());

        // Validation des données
        validerDisponibilite(disponibiliteDTO);

        // Vérifier que l'enseignant existe
        Enseignant enseignant = enseignantRepository.findById(disponibiliteDTO.getEnseignantId())
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + disponibiliteDTO.getEnseignantId()));

        // Vérifier les chevauchements
        verifierChevauchements(disponibiliteDTO, null);

        // Créer l'entité
        Disponibilite disponibilite = new Disponibilite(
                disponibiliteDTO.getJourSemaine(),
                disponibiliteDTO.getHeureDebut(),
                disponibiliteDTO.getHeureFin(),
                enseignant
        );

        // Sauvegarder
        Disponibilite savedDisponibilite = disponibiliteRepository.save(disponibilite);
        log.info("Disponibilité créée avec succès, ID: {}", savedDisponibilite.getId());

        return convertToDTO(savedDisponibilite);
    }

    /**
     * Met à jour une disponibilité existante
     */
    public DisponibiliteDTO modifierDisponibilite(Long id, DisponibiliteDTO disponibiliteDTO) {
        log.info("Modification de la disponibilité ID: {}", id);

        // Validation des données
        validerDisponibilite(disponibiliteDTO);

        // Vérifier que la disponibilité existe
        Disponibilite disponibilite = disponibiliteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Disponibilité non trouvée avec l'ID: " + id));

        // Vérifier les chevauchements (en excluant la disponibilité actuelle)
        disponibiliteDTO.setEnseignantId(disponibilite.getEnseignant().getIdUser());
        verifierChevauchements(disponibiliteDTO, id);

        // Mettre à jour les champs
        disponibilite.setJourSemaine(disponibiliteDTO.getJourSemaine());
        disponibilite.setHeureDebut(disponibiliteDTO.getHeureDebut());
        disponibilite.setHeureFin(disponibiliteDTO.getHeureFin());

        // Sauvegarder
        Disponibilite savedDisponibilite = disponibiliteRepository.save(disponibilite);
        log.info("Disponibilité modifiée avec succès, ID: {}", savedDisponibilite.getId());

        return convertToDTO(savedDisponibilite);
    }

    /**
     * Supprime une disponibilité
     */
    public void supprimerDisponibilite(Long id) {
        log.info("Suppression de la disponibilité ID: {}", id);

        if (!disponibiliteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Disponibilité non trouvée avec l'ID: " + id);
        }

        disponibiliteRepository.deleteById(id);
        log.info("Disponibilité supprimée avec succès, ID: {}", id);
    }

    /**
     * Récupère toutes les disponibilités d'un enseignant
     */
    @Transactional(readOnly = true)
    public List<DisponibiliteDTO> getDisponibilitesEnseignant(String enseignantId) {
        log.info("Récupération des disponibilités pour l'enseignant ID: {}", enseignantId);

        // Vérifier que l'enseignant existe
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }

        List<Disponibilite> disponibilites = disponibiliteRepository.findByEnseignantIdUserOrderByJourSemaineAscHeureDebutAsc(enseignantId);
        return disponibilites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les disponibilités d'un enseignant pour un jour donné
     */
    @Transactional(readOnly = true)
    public List<DisponibiliteDTO> getDisponibilitesEnseignantParJour(String enseignantId, DayOfWeek jourSemaine) {
        log.info("Récupération des disponibilités pour l'enseignant ID: {} le {}", enseignantId, jourSemaine);

        // Vérifier que l'enseignant existe
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }

        List<Disponibilite> disponibilites = disponibiliteRepository.findByEnseignantIdUserAndJourSemaineOrderByHeureDebutAsc(enseignantId, jourSemaine);
        return disponibilites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Trouve les enseignants disponibles à un créneau donné
     */
    @Transactional(readOnly = true)
    public List<String> getEnseignantsDisponibles(DayOfWeek jourSemaine, LocalTime heureDebut, LocalTime heureFin) {
        log.info("Recherche des enseignants disponibles le {} de {} à {}", jourSemaine, heureDebut, heureFin);

        if (heureDebut.isAfter(heureFin)) {
            throw new ValidationException("L'heure de début doit être antérieure à l'heure de fin");
        }

        List<Enseignant> enseignants = disponibiliteRepository.findEnseignantsDisponibles(jourSemaine, heureDebut, heureFin);
        return enseignants.stream()
                .map(Enseignant::getIdUser)
                .collect(Collectors.toList());
    }

    /**
     * Supprime toutes les disponibilités d'un enseignant
     */
    public void supprimerToutesDisponibilitesEnseignant(String enseignantId) {
        log.info("Suppression de toutes les disponibilités pour l'enseignant ID: {}", enseignantId);

        // Vérifier que l'enseignant existe
        if (!enseignantRepository.existsById(enseignantId)) {
            throw new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + enseignantId);
        }

        disponibiliteRepository.deleteByEnseignantIdUser(enseignantId);
        log.info("Toutes les disponibilités supprimées pour l'enseignant ID: {}", enseignantId);
    }

    /**
     * Valide les données d'une disponibilité
     */
    private void validerDisponibilite(DisponibiliteDTO disponibiliteDTO) {
        if (disponibiliteDTO.getJourSemaine() == null) {
            throw new ValidationException("Le jour de la semaine est obligatoire");
        }

        if (disponibiliteDTO.getHeureDebut() == null) {
            throw new ValidationException("L'heure de début est obligatoire");
        }

        if (disponibiliteDTO.getHeureFin() == null) {
            throw new ValidationException("L'heure de fin est obligatoire");
        }

        if (!disponibiliteDTO.isHeuresValides()) {
            throw new ValidationException("L'heure de début doit être antérieure à l'heure de fin");
        }

        if (disponibiliteDTO.getEnseignantId() == null) {
            throw new ValidationException("L'ID de l'enseignant est obligatoire");
        }
    }

    /**
     * Vérifie les chevauchements avec d'autres disponibilités
     */
    private void verifierChevauchements(DisponibiliteDTO disponibiliteDTO, Long excludeId) {
        Long excludeIdSafe = excludeId != null ? excludeId : -1L;
        
        List<Disponibilite> chevauchements = disponibiliteRepository.findChevauchements(
                disponibiliteDTO.getEnseignantId(),
                disponibiliteDTO.getJourSemaine(),
                disponibiliteDTO.getHeureDebut(),
                disponibiliteDTO.getHeureFin(),
                excludeIdSafe
        );

        if (!chevauchements.isEmpty()) {
            throw new ValidationException("Ce créneau chevauche avec une disponibilité existante");
        }
    }

    /**
     * Convertit une entité Disponibilite en DTO
     */
    private DisponibiliteDTO convertToDTO(Disponibilite disponibilite) {
        return new DisponibiliteDTO(
                disponibilite.getId(),
                disponibilite.getJourSemaine(),
                disponibilite.getHeureDebut(),
                disponibilite.getHeureFin(),
                disponibilite.getEnseignant().getIdUser()
        );
    }
}