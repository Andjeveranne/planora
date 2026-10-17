package com.isi4.userservice.service;

import com.isi4.userservice.dto.EmploiDuTempsDTO;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.EmploiDuTemps;
import com.isi4.userservice.model.Enseignant;
import com.isi4.userservice.repository.EmploiDuTempsRepository;
import com.isi4.userservice.repository.EnseignantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des emplois du temps
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmploiDuTempsService {

    private final EmploiDuTempsRepository emploiDuTempsRepository;
    private final EnseignantRepository enseignantRepository;

    /**
     * Récupère tous les emplois du temps d'un enseignant
     */
    public List<EmploiDuTempsDTO> getEmploisDuTempsByEnseignant(String enseignantId) {
        log.info("Récupération des emplois du temps pour l'enseignant: {}", enseignantId);
        
        List<EmploiDuTemps> emplois = emploiDuTempsRepository.findByEnseignantIdOrderByDateJourAscHeureDebutAsc(enseignantId);
        return emplois.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les emplois du temps d'un enseignant pour une période donnée
     */
    public List<EmploiDuTempsDTO> getEmploisDuTempsByEnseignantAndPeriode(String enseignantId, LocalDate dateDebut, LocalDate dateFin) {
        log.info("Récupération des emplois du temps pour l'enseignant {} entre {} et {}", enseignantId, dateDebut, dateFin);
        
        List<EmploiDuTemps> emplois = emploiDuTempsRepository.findByEnseignantIdAndDateJourBetween(enseignantId, dateDebut, dateFin);
        return emplois.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crée un nouvel emploi du temps
     */
    @Transactional
    public EmploiDuTempsDTO createEmploiDuTemps(EmploiDuTempsDTO emploiDTO) {
        log.info("Création d'un nouvel emploi du temps pour l'enseignant: {}", emploiDTO.getEnseignantId());
        
        // Validation
        validateEmploiDuTemps(emploiDTO);
        
        // Vérification des conflits
        checkConflicts(emploiDTO);
        
        // Récupération de l'enseignant
        Enseignant enseignant = enseignantRepository.findById(emploiDTO.getEnseignantId())
                .orElseThrow(() -> new ResourceNotFoundException("Enseignant non trouvé avec l'ID: " + emploiDTO.getEnseignantId()));
        
        // Création de l'entité
        EmploiDuTemps emploi = convertToEntity(emploiDTO, enseignant);
        
        // Sauvegarde
        EmploiDuTemps savedEmploi = emploiDuTempsRepository.save(emploi);
        
        log.info("Emploi du temps créé avec succès avec l'ID: {}", savedEmploi.getId());
        return convertToDTO(savedEmploi);
    }

    /**
     * Met à jour un emploi du temps existant
     */
    @Transactional
    public EmploiDuTempsDTO updateEmploiDuTemps(Long id, EmploiDuTempsDTO emploiDTO) {
        log.info("Mise à jour de l'emploi du temps avec l'ID: {}", id);
        
        EmploiDuTemps existingEmploi = emploiDuTempsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emploi du temps non trouvé avec l'ID: " + id));
        
        // Validation
        validateEmploiDuTemps(emploiDTO);
        
        // Vérification des conflits (en excluant l'emploi actuel)
        checkConflictsForUpdate(id, emploiDTO);
        
        // Mise à jour des champs
        updateEmploiFields(existingEmploi, emploiDTO);
        
        // Sauvegarde
        EmploiDuTemps updatedEmploi = emploiDuTempsRepository.save(existingEmploi);
        
        log.info("Emploi du temps mis à jour avec succès");
        return convertToDTO(updatedEmploi);
    }

    /**
     * Supprime un emploi du temps
     */
    @Transactional
    public void deleteEmploiDuTemps(Long id) {
        log.info("Suppression de l'emploi du temps avec l'ID: {}", id);
        
        if (!emploiDuTempsRepository.existsById(id)) {
            throw new ResourceNotFoundException("Emploi du temps non trouvé avec l'ID: " + id);
        }
        
        emploiDuTempsRepository.deleteById(id);
        log.info("Emploi du temps supprimé avec succès");
    }

    /**
     * Supprime tous les emplois du temps d'un enseignant
     */
    @Transactional
    public void deleteAllEmploisDuTempsByEnseignant(String enseignantId) {
        log.info("Suppression de tous les emplois du temps pour l'enseignant: {}", enseignantId);
        
        emploiDuTempsRepository.deleteByEnseignantId(enseignantId);
        log.info("Tous les emplois du temps supprimés pour l'enseignant: {}", enseignantId);
    }

    /**
     * Récupère les emplois du temps par salle
     */
    public List<EmploiDuTempsDTO> getEmploisDuTempsBySalle(String salle) {
        log.info("Récupération des emplois du temps pour la salle: {}", salle);
        
        List<EmploiDuTemps> emplois = emploiDuTempsRepository.findBySalle(salle);
        return emplois.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les emplois du temps par classe
     */
    public List<EmploiDuTempsDTO> getEmploisDuTempsByClasse(String classe) {
        log.info("Récupération des emplois du temps pour la classe: {}", classe);
        
        List<EmploiDuTemps> emplois = emploiDuTempsRepository.findByClasse(classe);
        return emplois.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Méthodes privées de validation et conversion

    private void validateEmploiDuTemps(EmploiDuTempsDTO emploiDTO) {
        if (emploiDTO.getEnseignantId() == null || emploiDTO.getEnseignantId().trim().isEmpty()) {
            throw new ValidationException("L'ID de l'enseignant est obligatoire");
        }
        
        if (emploiDTO.getDateJour() == null) {
            throw new ValidationException("La date du jour est obligatoire");
        }
        
        if (emploiDTO.getHeureDebut() == null || emploiDTO.getHeureFin() == null) {
            throw new ValidationException("Les heures de début et de fin sont obligatoires");
        }
        
        if (emploiDTO.getHeureDebut() >= emploiDTO.getHeureFin()) {
            throw new ValidationException("L'heure de début doit être antérieure à l'heure de fin");
        }
        
        if (emploiDTO.getUe() == null || emploiDTO.getUe().trim().isEmpty()) {
            throw new ValidationException("L'UE est obligatoire");
        }
        
        if (emploiDTO.getClasse() == null || emploiDTO.getClasse().trim().isEmpty()) {
            throw new ValidationException("La classe est obligatoire");
        }
        
        if (emploiDTO.getSalle() == null || emploiDTO.getSalle().trim().isEmpty()) {
            throw new ValidationException("La salle est obligatoire");
        }
        
        if (emploiDTO.getTypeCours() == null || emploiDTO.getTypeCours().trim().isEmpty()) {
            throw new ValidationException("Le type de cours est obligatoire");
        }
    }

    private void checkConflicts(EmploiDuTempsDTO emploiDTO) {
        // Vérifier les conflits d'enseignant
        List<EmploiDuTemps> conflictsEnseignant = emploiDuTempsRepository.findConflictsForEnseignant(
                emploiDTO.getEnseignantId(), emploiDTO.getDateJour(), 
                emploiDTO.getHeureDebut(), emploiDTO.getHeureFin());
        
        if (!conflictsEnseignant.isEmpty()) {
            throw new ValidationException("Conflit d'horaire détecté pour l'enseignant");
        }
        
        // Vérifier les conflits de salle
        List<EmploiDuTemps> conflictsSalle = emploiDuTempsRepository.findConflictsForSalle(
                emploiDTO.getSalle(), emploiDTO.getDateJour(), 
                emploiDTO.getHeureDebut(), emploiDTO.getHeureFin());
        
        if (!conflictsSalle.isEmpty()) {
            throw new ValidationException("Conflit d'occupation de salle détecté");
        }
    }

    private void checkConflictsForUpdate(Long excludeId, EmploiDuTempsDTO emploiDTO) {
        // Vérifier les conflits d'enseignant (en excluant l'emploi actuel)
        List<EmploiDuTemps> conflictsEnseignant = emploiDuTempsRepository.findConflictsForEnseignant(
                emploiDTO.getEnseignantId(), emploiDTO.getDateJour(), 
                emploiDTO.getHeureDebut(), emploiDTO.getHeureFin());
        
        conflictsEnseignant = conflictsEnseignant.stream()
                .filter(e -> !e.getId().equals(excludeId))
                .collect(Collectors.toList());
        
        if (!conflictsEnseignant.isEmpty()) {
            throw new ValidationException("Conflit d'horaire détecté pour l'enseignant");
        }
        
        // Vérifier les conflits de salle (en excluant l'emploi actuel)
        List<EmploiDuTemps> conflictsSalle = emploiDuTempsRepository.findConflictsForSalle(
                emploiDTO.getSalle(), emploiDTO.getDateJour(), 
                emploiDTO.getHeureDebut(), emploiDTO.getHeureFin());
        
        conflictsSalle = conflictsSalle.stream()
                .filter(e -> !e.getId().equals(excludeId))
                .collect(Collectors.toList());
        
        if (!conflictsSalle.isEmpty()) {
            throw new ValidationException("Conflit d'occupation de salle détecté");
        }
    }

    private void updateEmploiFields(EmploiDuTemps emploi, EmploiDuTempsDTO emploiDTO) {
        emploi.setDateJour(emploiDTO.getDateJour());
        emploi.setJour(emploiDTO.getJour());
        emploi.setHeureDebut(emploiDTO.getHeureDebut());
        emploi.setHeureFin(emploiDTO.getHeureFin());
        emploi.setUe(emploiDTO.getUe());
        emploi.setClasse(emploiDTO.getClasse());
        emploi.setSalle(emploiDTO.getSalle());
        emploi.setTypeCours(emploiDTO.getTypeCours());
        emploi.setUpdatedAt(LocalDateTime.now());
    }

    private EmploiDuTemps convertToEntity(EmploiDuTempsDTO dto, Enseignant enseignant) {
        EmploiDuTemps emploi = new EmploiDuTemps();
        emploi.setDateJour(dto.getDateJour());
        emploi.setJour(dto.getJour());
        emploi.setHeureDebut(dto.getHeureDebut());
        emploi.setHeureFin(dto.getHeureFin());
        emploi.setUe(dto.getUe());
        emploi.setClasse(dto.getClasse());
        emploi.setSalle(dto.getSalle());
        emploi.setTypeCours(dto.getTypeCours());
        emploi.setEnseignant(enseignant);
        
        LocalDateTime now = LocalDateTime.now();
        emploi.setCreatedAt(now);
        emploi.setUpdatedAt(now);
        
        return emploi;
    }

    private EmploiDuTempsDTO convertToDTO(EmploiDuTemps emploi) {
        EmploiDuTempsDTO dto = new EmploiDuTempsDTO();
        dto.setId(emploi.getId());
        dto.setEnseignantId(emploi.getEnseignant() != null ? emploi.getEnseignant().getIdUser() : null);
        dto.setDateJour(emploi.getDateJour());
        dto.setJour(emploi.getJour());
        dto.setHeureDebut(emploi.getHeureDebut());
        dto.setHeureFin(emploi.getHeureFin());
        dto.setUe(emploi.getUe());
        dto.setClasse(emploi.getClasse());
        dto.setSalle(emploi.getSalle());
        dto.setTypeCours(emploi.getTypeCours());
        dto.setCreatedAt(emploi.getCreatedAt());
        dto.setUpdatedAt(emploi.getUpdatedAt());
        
        return dto;
    }
}