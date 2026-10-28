package com.isi4.planningservice.repository.scope;

import com.isi4.planningservice.entity.scope.AcademicLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, String> {
    List<AcademicLevel> findBySpecialityId(String specialityId);
}
