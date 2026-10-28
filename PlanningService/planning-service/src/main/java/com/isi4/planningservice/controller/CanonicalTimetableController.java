package com.isi4.planningservice.controller;

import com.isi4.planningservice.dto.GenerationRequestDto;
import com.isi4.planningservice.dto.GenerationSummaryDto;
import com.isi4.planningservice.service.CanonicalTimetableGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
@RequiredArgsConstructor
@Slf4j
public class CanonicalTimetableController {

    private final CanonicalTimetableGenerationService canonicalTimetableGenerationService;

    @PostMapping("/canonical/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GenerationSummaryDto> generate(@RequestBody GenerationRequestDto request) {
        log.info("Incoming canonical timetable generation request: school={}, department={}, speciality={}, level={}",
                request.getSchoolId(), request.getDepartmentId(), request.getSpecialityId(), request.getLevelId());
        GenerationSummaryDto summary = canonicalTimetableGenerationService.generate(request);
        return ResponseEntity.ok(summary);
    }
}
