package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.GenerationRun;
import com.isi4.planningservice.entity.GenerationRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenerationRunRepository extends JpaRepository<GenerationRun, Long> {
    List<GenerationRun> findByStatus(GenerationRunStatus status);
}
