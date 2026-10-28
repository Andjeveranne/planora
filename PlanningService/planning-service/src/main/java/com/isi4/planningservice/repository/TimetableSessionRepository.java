package com.isi4.planningservice.repository;

import com.isi4.planningservice.entity.TimetableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableSessionRepository extends JpaRepository<TimetableSession, Long> {
    List<TimetableSession> findByGenerationRunId(Long generationRunId);
}
