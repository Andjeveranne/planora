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

    @Test
    void doesNotScheduleTeacherOutsideDeclaredAvailability() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 8, 0);
        FordFulkersonScheduler.SchedulingRequest request = request();
        FordFulkersonScheduler.TimeSlot slot = slot(start);

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request), List.of(slot), List.of(room("room-ict", "ict", 30)), List.of());

        assertTrue(results.isEmpty());
    }

    @Test
    void doesNotScheduleTeacherWhenAvailabilityCoversOnlyPartOfSlot() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 5, 8, 0);
        FordFulkersonScheduler.Disponibilite availability = new FordFulkersonScheduler.Disponibilite();
        availability.setEnseignantId("teacher-1");
        availability.setDebut(start);
        availability.setFin(start.plusHours(1));

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request()), List.of(slot(start)), List.of(room("room-ict", "ict", 30)), List.of(availability));

        assertTrue(results.isEmpty());
    }

    @Test
    void generatesCourseForLiveNetworkingClassWhenRoomAndAvailabilityAreCompatible() {
        LocalDateTime start = LocalDateTime.of(2026, 10, 19, 8, 0);
        FordFulkersonScheduler.SchedulingRequest request = new FordFulkersonScheduler.SchedulingRequest();
        request.setClasseId("eaf64cfe-eb69-496b-b994-f9be177407fd");
        request.setIdEcole("ICTU");
        request.setUeId(21L);
        request.setEnseignantId("09beed87-b406-4e77-9a9b-25cd209d39aa");
        request.setTypeCours("CM");
        request.setHeuresRequises(2);
        request.setEffectifClasse(30);

        FordFulkersonScheduler.Disponibilite availability = new FordFulkersonScheduler.Disponibilite();
        availability.setEnseignantId(request.getEnseignantId());
        availability.setDebut(start);
        availability.setFin(start.plusHours(2));

        FordFulkersonScheduler.Salle temporaryTestRoom = room("temporary-ictu-test-room", "ICTU", 30);
        temporaryTestRoom.setType("SALLE_COURS");

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request), List.of(slot(start)), List.of(temporaryTestRoom), List.of(availability));

        assertEquals(1, results.size());
        assertEquals(request.getClasseId(), results.get(0).getClasseId());
        assertEquals(request.getEnseignantId(), results.get(0).getEnseignantId());
        assertEquals(request.getUeId(), results.get(0).getUeId());
        assertEquals(temporaryTestRoom.getId(), results.get(0).getSalleId());
        assertEquals(start, results.get(0).getDateHeureDebut());
        assertEquals(start.plusHours(2), results.get(0).getDateHeureFin());
    }

    @Test
    void onlyUsesClassAssignedRoomForClassSpecificRooms() {
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

        FordFulkersonScheduler.Salle assignedElsewhere = room("room-other-class", "ict", 30);
        assignedElsewhere.setIdClasse("class-other");
        FordFulkersonScheduler.Salle assignedToClass = room("room-class", "ict", 30);
        assignedToClass.setIdClasse("class-ict");

        List<FordFulkersonScheduler.SchedulingResult> results = scheduler.generateSchedule(
                List.of(request), List.of(slot), List.of(assignedElsewhere, assignedToClass), List.of(availability));

        assertEquals(1, results.size());
        assertEquals("room-class", results.get(0).getSalleId());
    }

    private FordFulkersonScheduler.SchedulingRequest request() {
        FordFulkersonScheduler.SchedulingRequest request = new FordFulkersonScheduler.SchedulingRequest();
        request.setClasseId("class-ict");
        request.setIdEcole("ict");
        request.setUeId(1L);
        request.setEnseignantId("teacher-1");
        request.setTypeCours("CM");
        request.setHeuresRequises(2);
        request.setEffectifClasse(25);
        return request;
    }

    private FordFulkersonScheduler.TimeSlot slot(LocalDateTime start) {
        FordFulkersonScheduler.TimeSlot slot = new FordFulkersonScheduler.TimeSlot();
        slot.setDebut(start);
        slot.setFin(start.plusHours(2));
        slot.setJourSemaine(1);
        slot.setCreneau("8h-10h");
        slot.setSemaine(41);
        return slot;
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
