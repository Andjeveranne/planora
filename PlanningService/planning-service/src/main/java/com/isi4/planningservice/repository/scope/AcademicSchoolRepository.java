package com.isi4.planningservice.repository.scope;

import com.isi4.planningservice.entity.scope.AcademicSchool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicSchoolRepository extends JpaRepository<AcademicSchool, String> {
}
