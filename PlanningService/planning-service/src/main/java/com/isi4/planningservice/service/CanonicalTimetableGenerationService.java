package com.isi4.planningservice.service;

import com.isi4.planningservice.dto.GenerationRequestDto;
import com.isi4.planningservice.dto.GenerationSummaryDto;
import com.isi4.planningservice.entity.GenerationRun;
import com.isi4.planningservice.entity.GenerationRunStatus;
import com.isi4.planningservice.entity.TimetableSession;
import com.isi4.planningservice.repository.GenerationRunRepository;
import com.isi4.planningservice.repository.TimetableSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CanonicalTimetableGenerationService {

    private final GenerationRunRepository generationRunRepository;
    private final TimetableSessionRepository timetableSessionRepository;
    private final AcademicScopeService academicScopeService;

    @Transactional
    public GenerationSummaryDto generate(GenerationRequestDto request) {
        validateRequest(request);

        if (!academicScopeService.isScopeValid(
                request.getSchoolId(),
                request.getDepartmentId(),
                request.getSpecialityId(),
                request.getLevelId())) {
            throw new IllegalArgumentException("Invalid academic scope: schoolId, departmentId, specialityId and levelId must belong to the same canonical hierarchy.");
        }

        GenerationRun run = new GenerationRun();
        run.setSchoolId(request.getSchoolId());
        run.setDepartmentId(request.getDepartmentId());
        run.setSpecialityId(request.getSpecialityId());
        run.setLevelId(request.getLevelId());
        run.setGeneratedBy(request.getGeneratedBy());
        run.setStatus(GenerationRunStatus.RUNNING);
        run.setStartedAt(LocalDateTime.now());
        run.setNotes("Canonical timetable generation started with scoped IDs");

        GenerationRun savedRun = generationRunRepository.save(run);
        log.info("Canonical generation run created: {} for school={}, dept={}, specialty={}, level={}",
                savedRun.getId(), savedRun.getSchoolId(), savedRun.getDepartmentId(),
                savedRun.getSpecialityId(), savedRun.getLevelId());

        List<TimetableSession> sessions = new ArrayList<>();

        // This is the new canonical model: generation is based on explicit IDs and compatibility rules,
        // not fuzzy string matching between department/speciality labels from different services.
        // The actual catalog resolution and constraint-solving can be added in the next step once the
        // data source contracts are fully aligned to canonical IDs.

        savedRun.setStatus(GenerationRunStatus.SUCCESS);
        savedRun.setFinishedAt(LocalDateTime.now());
        savedRun.setNotes("Canonical timetable generation contract accepted; scope was validated and the planner is ready to populate timetable sessions from canonical IDs.");
        generationRunRepository.save(savedRun);

        if (!sessions.isEmpty()) {
            timetableSessionRepository.saveAll(sessions);
        }

        return new GenerationSummaryDto(
                savedRun.getId(),
                savedRun.getSchoolId(),
                savedRun.getDepartmentId(),
                savedRun.getSpecialityId(),
                savedRun.getLevelId(),
                savedRun.getStatus().name(),
                sessions.size(),
                0
        );
    }

    private void validateRequest(GenerationRequestDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Generation request is required");
        }

        if (request.getSchoolId() == null || request.getSchoolId().isBlank()) {
            throw new IllegalArgumentException("schoolId is required");
        }
        if (request.getDepartmentId() == null || request.getDepartmentId().isBlank()) {
            throw new IllegalArgumentException("departmentId is required");
        }
        if (request.getSpecialityId() == null || request.getSpecialityId().isBlank()) {
            throw new IllegalArgumentException("specialityId is required");
        }
        if (request.getLevelId() == null || request.getLevelId().isBlank()) {
            throw new IllegalArgumentException("levelId is required");
        }
        if (request.getGeneratedBy() == null || request.getGeneratedBy().isBlank()) {
            throw new IllegalArgumentException("generatedBy is required");
        }
    }
}
