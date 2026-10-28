package com.isi4.planningservice.service;

import com.isi4.planningservice.entity.scope.*;
import com.isi4.planningservice.repository.scope.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AcademicScopeService {

    private final AcademicSchoolRepository academicSchoolRepository;
    private final AcademicDepartmentRepository academicDepartmentRepository;
    private final AcademicSpecialityRepository academicSpecialityRepository;
    private final AcademicLevelRepository academicLevelRepository;
    private final AcademicGroupRepository academicGroupRepository;

    public Optional<AcademicSchool> getSchool(String schoolId) {
        return academicSchoolRepository.findById(schoolId);
    }

    public List<AcademicDepartment> getDepartmentsBySchool(String schoolId) {
        return academicDepartmentRepository.findBySchoolId(schoolId);
    }

    public List<AcademicSpeciality> getSpecialitiesByDepartment(String departmentId) {
        return academicSpecialityRepository.findByDepartmentId(departmentId);
    }

    public List<AcademicLevel> getLevelsBySpeciality(String specialityId) {
        return academicLevelRepository.findBySpecialityId(specialityId);
    }

    public List<AcademicGroup> getGroupsByLevel(String levelId) {
        return academicGroupRepository.findByLevelId(levelId);
    }

    public boolean isScopeValid(String schoolId, String departmentId, String specialityId, String levelId) {
        if (schoolId == null || departmentId == null || specialityId == null || levelId == null) {
            return false;
        }

        Optional<AcademicSchool> school = academicSchoolRepository.findById(schoolId);
        if (school.isEmpty() || !school.get().isActive()) {
            return false;
        }

        Optional<AcademicDepartment> department = academicDepartmentRepository.findById(departmentId);
        if (department.isEmpty() || !department.get().isActive() || !schoolId.equals(department.get().getSchoolId())) {
            return false;
        }

        Optional<AcademicSpeciality> speciality = academicSpecialityRepository.findById(specialityId);
        if (speciality.isEmpty() || !speciality.get().isActive() || !departmentId.equals(speciality.get().getDepartmentId())) {
            return false;
        }

        Optional<AcademicLevel> level = academicLevelRepository.findById(levelId);
        return level.isPresent() && level.get().isActive() && specialityId.equals(level.get().getSpecialityId());
    }
}
