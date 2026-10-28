package com.isi4.userservice.repository;

import com.isi4.userservice.model.AttendanceRecord;
import com.isi4.userservice.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findBySessionIdOrderByScannedAtDesc(Long sessionId);

    Optional<AttendanceRecord> findBySessionIdAndStudentId(Long sessionId, String studentId);

    List<AttendanceRecord> findByStudentIdOrderByScannedAtDesc(String studentId);

    List<AttendanceRecord> findBySession_Course_Id(Long courseId);

    long countBySessionId(Long sessionId);

    long countBySessionIdAndStatus(Long sessionId, AttendanceStatus status);

    long countByStudentIdAndStatus(String studentId, AttendanceStatus status);

    long countByStudentId(String studentId);
}

