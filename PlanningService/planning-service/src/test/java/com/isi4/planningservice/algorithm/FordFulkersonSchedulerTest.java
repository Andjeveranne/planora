package com.isi4.planningservice.algorithm;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FordFulkersonSchedulerTest {

    private final FordFulkersonScheduler scheduler = new FordFulkersonScheduler();

    @Test
    void assignsClassOnlyToRoomFromItsSchool() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 8, 0);

        FordFulkersonScheduler.SchedulingRequest request = new FordFulkersonScheduler.SchedulingRequest();
        request.setClasseId("class-ict");
        request.setIdEcole("ict");
        request.setUeId(1L);
        request.setEnseignantId("teacher-1");
        request.setTypeCours("CM");
        request.setHeuresRequises(2);
        request.setEffectifClasse(25);

        FordFulkersonScheduler.TimeSlot slot = new FordFulkersonScheduler.TimeSlot();
        slot.setDebut(start);
        slot.setFin(start.plusHours(2));
        slot.setJourSemaine(1);
        slot.setCreneau("8h-10h");
        slot.setSemaine(41);

        FordFulkersonScheduler.Salle otherSchoolRoom = room("room-other", "other", 100);
        FordFulkersonScheduler.Salle ictRoom = room("room-ict", "ict", 30);
        FordFulkersonScheduler.Disponibilite availability = new FordFulkersonScheduler.Disponibilite();
        availability.setEnseignantId("teacher-1");
        availability.setDebut(start);
        availability.setFin(start.plusHours(2));

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request), List.of(slot), List.of(otherSchoolRoom, ictRoom), List.of(availability));

        assertEquals(1, results.size());
        assertEquals("room-ict", results.get(0).getSalleId());
    }

    @Test
    void doesNotScheduleWhenSchoolHasNoCompatibleRoom() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 8, 0);

        FordFulkersonScheduler.SchedulingRequest request = new FordFulkersonScheduler.SchedulingRequest();
        request.setClasseId("class-ict");
        request.setIdEcole("ict");
        request.setUeId(1L);
        request.setEnseignantId("teacher-1");
        request.setTypeCours("CM");
        request.setHeuresRequises(2);
        request.setEffectifClasse(25);

        FordFulkersonScheduler.TimeSlot slot = new FordFulkersonScheduler.TimeSlot();
        slot.setDebut(start);
        slot.setFin(start.plusHours(2));
        slot.setJourSemaine(1);
        slot.setCreneau("8h-10h");
        slot.setSemaine(41);

        FordFulkersonScheduler.Disponibilite availability = new FordFulkersonScheduler.Disponibilite();
        availability.setEnseignantId("teacher-1");
        availability.setDebut(start);
        availability.setFin(start.plusHours(2));

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request), List.of(slot), List.of(room("room-other", "other", 100)), List.of(availability));

        assertTrue(results.isEmpty());
    }

    private FordFulkersonScheduler.Salle room(String id, String schoolId, int capacity) {
        FordFulkersonScheduler.Salle room = new FordFulkersonScheduler.Salle();
        room.setId(id);
        room.setIdEcole(schoolId);
        room.setCapacite(capacity);
        room.setType("AMPHITHEATRE");
        return room;
    }
}
