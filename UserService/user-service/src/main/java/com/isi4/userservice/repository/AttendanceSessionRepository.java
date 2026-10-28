package com.isi4.userservice.repository;

import com.isi4.userservice.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {

    List<AttendanceSession> findByIsActiveTrueOrderByStartedAtDesc();

    List<AttendanceSession> findByTeacherIdAndIsActiveTrueOrderByStartedAtDesc(String teacherId);

    List<AttendanceSession> findByTeacherIdOrderByStartedAtDesc(String teacherId);

    List<AttendanceSession> findByCourseIdAndIsActiveTrue(Long courseId);

    List<AttendanceSession> findByCourseIdOrderByStartedAtDesc(Long courseId);

    Optional<AttendanceSession> findByQrTokenAndIsActiveTrue(String qrToken);

    Optional<AttendanceSession> findFirstByCourseIdAndIsActiveTrueOrderByStartedAtDesc(Long courseId);
}

