package com.isi4.planningservice.service;

import com.isi4.planningservice.dto.DisponibiliteDTO;
import com.isi4.planningservice.entity.Disponibilite;
import com.isi4.planningservice.repository.DisponibiliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;

    public List<DisponibiliteDTO> getDisponibilitesByEnseignant(String enseignantId) {
        log.info("Fetching disponibilites for teacher: {}", enseignantId);
        return disponibiliteRepository.findByEnseignantIdOrderByDateHeureDebutAsc(enseignantId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<DisponibiliteDTO> getDisponibilitesByEnseignantAndSchool(String enseignantId, String idEcole) {
        return disponibiliteRepository.findByEnseignantIdAndIdEcoleOrderByDateHeureDebutAsc(enseignantId, idEcole)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<DisponibiliteDTO> getDisponibilitesByEnseignantAndDateRange(String enseignantId, LocalDateTime start, LocalDateTime end) {
        log.info("Fetching disponibilites for teacher {} between {} and {}", enseignantId, start, end);
        return disponibiliteRepository.findByEnseignantIdAndDateHeureDebutBetween(enseignantId, start, end)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DisponibiliteDTO addDisponibilite(DisponibiliteDTO dto) {
        log.info("Adding disponibilite for teacher {} from {} to {}", 
                dto.getEnseignantId(), dto.getDateHeureDebut(), dto.getDateHeureFin());

        validateDisponibilite(dto);

        if (disponibiliteRepository.existsByEnseignantIdAndDateHeureDebutLessThanAndDateHeureFinGreaterThan(
                dto.getEnseignantId(), dto.getDateHeureFin(), dto.getDateHeureDebut())) {
            throw new IllegalArgumentException("Cet enseignant est déjà disponible dans une autre école sur ce créneau.");
        }

        Disponibilite disponibilite = new Disponibilite();
        disponibilite.setEnseignantId(dto.getEnseignantId());
        disponibilite.setIdEcole(dto.getIdEcole());
        disponibilite.setDateHeureDebut(dto.getDateHeureDebut());
        disponibilite.setDateHeureFin(dto.getDateHeureFin());
        disponibilite.setCommentaire(dto.getCommentaire());

        Disponibilite saved = disponibiliteRepository.save(disponibilite);
        log.info("Disponibilite saved with ID: {}", saved.getId());
        return toDTO(saved);
    }

    @Transactional
    public void deleteDisponibilite(Long id) {
        log.info("Deleting disponibilite with ID: {}", id);
        if (!disponibiliteRepository.existsById(id)) {
            throw new IllegalArgumentException("Disponibilite not found with ID: " + id);
        }
        disponibiliteRepository.deleteById(id);
        log.info("Disponibilite deleted successfully");
    }

    @Transactional
    public void deleteAllByEnseignant(String enseignantId) {
        log.info("Deleting all disponibilites for teacher: {}", enseignantId);
        disponibiliteRepository.deleteByEnseignantId(enseignantId);
    }

    private void validateDisponibilite(DisponibiliteDTO dto) {
        if (dto.getEnseignantId() == null || dto.getEnseignantId().isBlank()) {
            throw new IllegalArgumentException("enseignantId is required");
        }
        if (dto.getIdEcole() == null || dto.getIdEcole().isBlank()) {
            throw new IllegalArgumentException("idEcole is required");
        }
        if (dto.getDateHeureDebut() == null) {
            throw new IllegalArgumentException("dateHeureDebut is required");
        }
        if (dto.getDateHeureFin() == null) {
            throw new IllegalArgumentException("dateHeureFin is required");
        }
        if (dto.getDateHeureDebut().isAfter(dto.getDateHeureFin())) {
            throw new IllegalArgumentException("dateHeureDebut must be before dateHeureFin");
        }
        if (dto.getDateHeureDebut().equals(dto.getDateHeureFin())) {
            throw new IllegalArgumentException("dateHeureDebut and dateHeureFin cannot be the same");
        }
        if (!dto.getDateHeureDebut().toLocalDate().equals(dto.getDateHeureFin().toLocalDate())) {
            throw new IllegalArgumentException("dateHeureDebut and dateHeureFin must be on the same day");
        }
    }

    private DisponibiliteDTO toDTO(Disponibilite disponibilite) {
        return new DisponibiliteDTO(
                disponibilite.getId(),
                disponibilite.getEnseignantId(),
                disponibilite.getIdEcole(),
                disponibilite.getDateHeureDebut(),
                disponibilite.getDateHeureFin(),
                disponibilite.getCommentaire()
        );
    }

    public List<String> getEnseignantsAvecDisponibilites() {
        return disponibiliteRepository.findAll()
                .stream()
                .map(Disponibilite::getEnseignantId)
                .distinct()
                .collect(Collectors.toList());
    }
}
