package com.isi4.planningservice.service;

import com.isi4.planningservice.model.Disponibilite;
import com.isi4.planningservice.repository.DisponibiliteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisponibiliteService {

    private final DisponibiliteRepository disponibiliteRepository;
    
    // Business rules constants
    private static final LocalTime BUSINESS_START = LocalTime.of(8, 0);  // 8:00 AM
    private static final LocalTime BUSINESS_END = LocalTime.of(17, 0);   // 5:00 PM
    private static final int MIN_DURATION_MINUTES = 60; // Minimum 1 hour

    public List<Disponibilite> getDisponibilitesByEnseignant(String enseignantId) {
        return disponibiliteRepository.findByEnseignantIdOrderByDateHeureDebutAsc(enseignantId);
    }

    @Transactional
    public Disponibilite addDisponibilite(String enseignantId, LocalDateTime debut, LocalDateTime fin,
            String commentaire) {
        
        log.info("Adding availability for teacher {} from {} to {}", enseignantId, debut, fin);
        
        // Apply business logic validation
        validateAvailabilityBusinessRules(debut, fin);
        
        // Check for overlapping availabilities
        validateNoOverlap(enseignantId, debut, fin);

        Disponibilite dispo = new Disponibilite();
        dispo.setEnseignantId(enseignantId);
        dispo.setDateHeureDebut(debut);
        dispo.setDateHeureFin(fin);
        dispo.setCommentaire(commentaire);

        Disponibilite saved = disponibiliteRepository.save(dispo);
        log.info("Availability added successfully with ID: {}", saved.getId());
        
        return saved;
    }

    /**
     * Validates business rules for availability:
     * 1. No past dates
     * 2. Monday to Saturday only (no Sunday)
     * 3. Business hours only (8:00 AM to 5:00 PM)
     * 4. Minimum duration of 1 hour
     * 5. Start time before end time
     */
    private void validateAvailabilityBusinessRules(LocalDateTime debut, LocalDateTime fin) {
        // Rule 1: No past dates
        if (debut.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Les disponibilités ne peuvent pas être ajoutées pour des dates passées");
        }
        
        // Rule 2: Start time must be before end time
        if (debut.isAfter(fin) || debut.isEqual(fin)) {
            throw new IllegalArgumentException("L'heure de début doit être avant l'heure de fin");
        }
        
        // Rule 3: Monday to Saturday only (no Sunday)
        DayOfWeek dayOfWeek = debut.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Les disponibilités ne peuvent pas être ajoutées le dimanche");
        }
        
        // Rule 4: Business hours only (8:00 AM to 5:00 PM)
        LocalTime startTime = debut.toLocalTime();
        LocalTime endTime = fin.toLocalTime();
        
        if (startTime.isBefore(BUSINESS_START) || startTime.isAfter(BUSINESS_END)) {
            throw new IllegalArgumentException("L'heure de début doit être entre 8h00 et 17h00");
        }
        
        if (endTime.isBefore(BUSINESS_START) || endTime.isAfter(BUSINESS_END)) {
            throw new IllegalArgumentException("L'heure de fin doit être entre 8h00 et 17h00");
        }
        
        // Rule 5: Minimum duration of 1 hour
        long durationMinutes = java.time.Duration.between(debut, fin).toMinutes();
        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw new IllegalArgumentException("La durée minimale d'une disponibilité est de 1 heure");
        }
        
        // Rule 6: Same day validation (start and end must be on the same day)
        if (!debut.toLocalDate().equals(fin.toLocalDate())) {
            throw new IllegalArgumentException("Les heures de début et fin doivent être le même jour");
        }
        
        log.debug("Business rules validation passed for availability from {} to {}", debut, fin);
    }
    
    /**
     * Validates that the new availability doesn't overlap with existing ones
     */
    private void validateNoOverlap(String enseignantId, LocalDateTime debut, LocalDateTime fin) {
        List<Disponibilite> overlapping = disponibiliteRepository
            .findOverlappingAvailabilities(enseignantId, debut, fin);
        
        if (!overlapping.isEmpty()) {
            Disponibilite existing = overlapping.get(0);
            throw new IllegalArgumentException(
                String.format("Conflit détecté avec une disponibilité existante (%s - %s)", 
                    existing.getDateHeureDebut().toLocalTime(), 
                    existing.getDateHeureFin().toLocalTime()));
        }
    }
    
    /**
     * Checks if two time ranges overlap
     */
    private boolean hasTimeOverlap(LocalDateTime start1, LocalDateTime end1, 
                                  LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    @Transactional
    public void deleteDisponibilite(Long id) {
        if (!disponibiliteRepository.existsById(id)) {
            throw new RuntimeException("Disponibilité introuvable");
        }
        disponibiliteRepository.deleteById(id);
        log.info("Availability deleted with ID: {}", id);
    }
    
    /**
     * Get business rules information for frontend validation
     */
    public BusinessRulesInfo getBusinessRules() {
        return new BusinessRulesInfo(
            BUSINESS_START,
            BUSINESS_END,
            MIN_DURATION_MINUTES,
            List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY")
        );
    }
    
    /**
     * Business rules information DTO
     */
    public static class BusinessRulesInfo {
        public final LocalTime businessStart;
        public final LocalTime businessEnd;
        public final int minDurationMinutes;
        public final List<String> allowedDays;
        
        public BusinessRulesInfo(LocalTime businessStart, LocalTime businessEnd, 
                               int minDurationMinutes, List<String> allowedDays) {
            this.businessStart = businessStart;
            this.businessEnd = businessEnd;
            this.minDurationMinutes = minDurationMinutes;
            this.allowedDays = allowedDays;
        }
    }
}
