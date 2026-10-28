package com.isi4.planningservice.controller;

import com.isi4.planningservice.entity.scope.*;
import com.isi4.planningservice.service.AcademicScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/planning/scope")
@RequiredArgsConstructor
public class AcademicScopeController {

    private final AcademicScopeService academicScopeService;

    @GetMapping("/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Boolean>> validateScope(
            @RequestParam String schoolId,
            @RequestParam String departmentId,
            @RequestParam String specialityId,
            @RequestParam String levelId) {
        return ResponseEntity.ok(Map.of("valid", academicScopeService.isScopeValid(schoolId, departmentId, specialityId, levelId)));
    }

    @GetMapping("/schools/{schoolId}/departments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicDepartment>> getDepartments(@PathVariable String schoolId) {
        return ResponseEntity.ok(academicScopeService.getDepartmentsBySchool(schoolId));
    }

    @GetMapping("/departments/{departmentId}/specialities")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicSpeciality>> getSpecialities(@PathVariable String departmentId) {
        return ResponseEntity.ok(academicScopeService.getSpecialitiesByDepartment(departmentId));
    }

    @GetMapping("/specialities/{specialityId}/levels")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicLevel>> getLevels(@PathVariable String specialityId) {
        return ResponseEntity.ok(academicScopeService.getLevelsBySpeciality(specialityId));
    }

    @GetMapping("/levels/{levelId}/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AcademicGroup>> getGroups(@PathVariable String levelId) {
        return ResponseEntity.ok(academicScopeService.getGroupsByLevel(levelId));
    }
}
