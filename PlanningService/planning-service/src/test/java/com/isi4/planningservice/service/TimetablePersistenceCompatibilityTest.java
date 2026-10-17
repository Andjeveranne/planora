package com.isi4.planningservice.service;

import com.isi4.planningservice.algorithm.FordFulkersonScheduler;
import com.isi4.planningservice.entity.EmploiDuTemps;
import com.isi4.planningservice.repository.EmploiDuTempsRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimetablePersistenceCompatibilityTest {

    private final EmploiDuTempsRepository repository = mock(EmploiDuTempsRepository.class);
    private final EmploiDuTempsServiceV2 service =
            new EmploiDuTempsServiceV2(repository, null, null, null);

    @Test
    void mapsGeneratedCoursesToRequiredLegacyColumns() {
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmploiDuTemps saved = service.saveSchedulingResults(List.of(
                scheduledResult("class-1", "teacher-1", 42L, "room-1", "CM"))).get(0);

        assertEquals("class-1", saved.getClasse());
        assertEquals("42", saved.getUe());
        assertEquals("room-1", saved.getSalle());
        assertEquals("2026-10-19", saved.getDateJour().toString());
        assertEquals("lundi", saved.getJour());
        assertEquals(8, saved.getHeureDebut());
        assertEquals(10, saved.getHeureFin());
    }

    @Test
    void leavesTeacherAndUeReferencesNullForTpeBlocks() {
        when(repository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        EmploiDuTemps saved = service.saveSchedulingResults(List.of(
                scheduledResult("class-1", null, null, null, "TPE"))).get(0);

        assertNull(saved.getEnseignantId());
        assertNull(saved.getUeId());
        assertNull(saved.getSalleId());
        assertEquals("TPE", saved.getUe());
        assertEquals("TPE", saved.getSalle());
    }

    private static FordFulkersonScheduler.SchedulingResult scheduledResult(
            String classId, String teacherId, Long ueId, String roomId, String courseType) {
        FordFulkersonScheduler.SchedulingResult result =
                new FordFulkersonScheduler.SchedulingResult();
        result.setClasseId(classId);
        result.setEnseignantId(teacherId);
        result.setUeId(ueId);
        result.setSalleId(roomId);
        result.setDateHeureDebut(LocalDateTime.of(2026, 10, 19, 8, 0));
        result.setDateHeureFin(LocalDateTime.of(2026, 10, 19, 10, 0));
        result.setTypeCours(courseType);
        result.setSemaine(43);
        result.setJourSemaine(1);
        result.setCreneau("8h-10h");
        return result;
    }
}
