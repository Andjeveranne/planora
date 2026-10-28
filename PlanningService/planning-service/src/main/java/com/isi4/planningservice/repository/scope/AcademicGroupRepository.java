package com.isi4.planningservice.repository.scope;

import com.isi4.planningservice.entity.scope.AcademicGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicGroupRepository extends JpaRepository<AcademicGroup, String> {
    List<AcademicGroup> findByLevelId(String levelId);
}
