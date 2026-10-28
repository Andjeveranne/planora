package com.isi4.userservice.controller;

import com.isi4.userservice.dto.*;
import com.isi4.userservice.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/sessions/start")
    public ResponseEntity<AttendanceSessionDTO> startSession(
            @Valid @RequestBody StartSessionRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String teacherId = resolveUserId(headerUserId);
        log.info("Starting attendance session: courseId={}, teacherId={}", request.getCourseId(), teacherId);
        AttendanceSessionDTO session = attendanceService.startSession(request, teacherId);
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    @PostMapping("/sessions/{id}/regenerate-qr")
    public ResponseEntity<AttendanceSessionDTO> regenerateQrToken(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String teacherId = resolveUserId(headerUserId);
        log.info("Regenerating QR for session {}: teacherId={}", id, teacherId);
        AttendanceSessionDTO session = attendanceService.regenerateQrToken(id, teacherId);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/sessions/{id}/close")
    public ResponseEntity<AttendanceSessionDTO> closeSession(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String teacherId = resolveUserId(headerUserId);
        log.info("Closing session {}: teacherId={}", id, teacherId);
        AttendanceSessionDTO session = attendanceService.closeSession(id, teacherId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<List<AttendanceSessionDTO>> getActiveSessions(
            @RequestParam(required = false) String teacherId,
            @RequestParam(required = false) String studentId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestHeader(value = "X-User-Role", required = false) String headerUserRole) {

        String currentUserId = resolveUserId(headerUserId);
        String resolvedTeacherId = teacherId;
        if (resolvedTeacherId == null && "ENSEIGNANT".equalsIgnoreCase(headerUserRole)) {
            resolvedTeacherId = currentUserId;
        }

        List<AttendanceSessionDTO> active = attendanceService.getActiveSessions(resolvedTeacherId, studentId);
        return ResponseEntity.ok(active);
    }

    @GetMapping("/sessions/{id}/attendees")
    public ResponseEntity<List<AttendanceRecordDTO>> getSessionAttendees(@PathVariable Long id) {
        List<AttendanceRecordDTO> attendees = attendanceService.getSessionAttendees(id);
        return ResponseEntity.ok(attendees);
    }

    @PostMapping("/scan")
    public ResponseEntity<AttendanceRecordDTO> scanAttendance(
            @Valid @RequestBody ScanAttendanceRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        if (request.getStudentId() == null || request.getStudentId().isBlank()) {
            request.setStudentId(resolveUserId(headerUserId));
        }
        log.info("Processing scan attendance: student={}, token={}", request.getStudentId(), request.getQrToken());
        AttendanceRecordDTO record = attendanceService.scanAttendance(request);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/records/{id}/override")
    public ResponseEntity<AttendanceRecordDTO> overrideAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceOverrideRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String teacherId = resolveUserId(headerUserId);
        log.info("Overriding attendance record {}: status={}, teacher={}", id, request.getStatus(), teacherId);
        AttendanceRecordDTO record = attendanceService.overrideAttendance(id, request, teacherId);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/student/{studentId}/history")
    public ResponseEntity<List<AttendanceRecordDTO>> getStudentHistory(@PathVariable String studentId) {
        List<AttendanceRecordDTO> history = attendanceService.getStudentHistory(studentId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/courses/{courseId}/report")
    public ResponseEntity<CourseAttendanceReportDTO> getCourseReport(@PathVariable Long courseId) {
        CourseAttendanceReportDTO report = attendanceService.getCourseReport(courseId);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<CourseEnrollmentDTO> enrollInCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) String studentId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String targetStudentId = (studentId != null && !studentId.isBlank()) ? studentId : resolveUserId(headerUserId);
        log.info("Enrolling student {} in course {}", targetStudentId, courseId);
        CourseEnrollmentDTO enrollment = attendanceService.enrollStudent(courseId, targetStudentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    @DeleteMapping("/courses/{courseId}/enroll")
    public ResponseEntity<Void> unenrollFromCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) String studentId,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId) {
        String targetStudentId = (studentId != null && !studentId.isBlank()) ? studentId : resolveUserId(headerUserId);
        log.info("Unenrolling student {} from course {}", targetStudentId, courseId);
        attendanceService.unenrollStudent(courseId, targetStudentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/courses/student/{studentId}/enrolled")
    public ResponseEntity<List<CourseEnrollmentDTO>> getStudentEnrolledCourses(@PathVariable String studentId) {
        List<CourseEnrollmentDTO> enrollments = attendanceService.getStudentEnrolledCourses(studentId);
        return ResponseEntity.ok(enrollments);
    }

    private String resolveUserId(String headerUserId) {
        if (headerUserId != null && !headerUserId.isBlank()) {
            return headerUserId;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            return auth.getName();
        }
        return "TEACH-01";
    }
}

