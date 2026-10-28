package com.isi4.userservice.service;

import com.isi4.userservice.dto.*;
import com.isi4.userservice.exception.ResourceNotFoundException;
import com.isi4.userservice.exception.ValidationException;
import com.isi4.userservice.model.*;
import com.isi4.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceRecordRepository recordRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EtudiantRepository etudiantRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Transactional
    public AttendanceSessionDTO startSession(StartSessionRequest request, String teacherId) {
        log.info("Starting attendance session for course {} by teacher {}", request.getCourseId(), teacherId);

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + request.getCourseId()));

        // Close any currently active sessions for this course
        List<AttendanceSession> activeSessions = sessionRepository.findByCourseIdAndIsActiveTrue(course.getId());
        for (AttendanceSession active : activeSessions) {
            active.setIsActive(false);
            sessionRepository.save(active);
        }

        LocalDateTime now = LocalDateTime.now();
        int duration = (request.getDurationMinutes() != null && request.getDurationMinutes() > 0)
                ? request.getDurationMinutes() : 90;
        LocalDateTime expiresAt = now.plusMinutes(duration);

        String teacherName = "";
        if (course.getEnseignant() != null) {
            teacherName = (course.getEnseignant().getPrenomUser() != null ? course.getEnseignant().getPrenomUser() + " " : "")
                    + (course.getEnseignant().getNomUser() != null ? course.getEnseignant().getNomUser() : "");
        } else if (teacherId != null) {
            teacherName = teacherId;
        }

        String qrToken = "PLN-" + course.getId() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();

        AttendanceSession session = AttendanceSession.builder()
                .course(course)
                .teacherId(teacherId != null ? teacherId : (course.getEnseignant() != null ? course.getEnseignant().getIdUser() : "TEACH-01"))
                .teacherName(teacherName.trim().isEmpty() ? "Enseignant Planora" : teacherName.trim())
                .startedAt(now)
                .expiresAt(expiresAt)
                .qrToken(qrToken)
                .isActive(true)
                .room(request.getRoom() != null ? request.getRoom() : "Salle Principale")
                .sessionType(request.getSessionType() != null ? request.getSessionType() : course.getTypeCours())
                .build();

        AttendanceSession saved = sessionRepository.save(session);
        return mapToSessionDTO(saved);
    }

    @Transactional
    public AttendanceSessionDTO regenerateQrToken(Long sessionId, String teacherId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée avec l'ID: " + sessionId));

        if (!Boolean.TRUE.equals(session.getIsActive())) {
            throw new ValidationException("Impossible de régénérer le QR d'une session fermée");
        }

        String newToken = "PLN-" + session.getCourse().getId() + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        session.setQrToken(newToken);
        // Extend expiry if remaining time is less than 5 minutes
        if (session.getExpiresAt().isBefore(LocalDateTime.now().plusMinutes(5))) {
            session.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        }

        AttendanceSession updated = sessionRepository.save(session);
        return mapToSessionDTO(updated);
    }

    @Transactional
    public AttendanceSessionDTO closeSession(Long sessionId, String teacherId) {
        AttendanceSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session non trouvée avec l'ID: " + sessionId));

        session.setIsActive(false);
        AttendanceSession updated = sessionRepository.save(session);
        return mapToSessionDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDTO> getActiveSessions(String teacherId, String studentId) {
        List<AttendanceSession> sessions;
        if (teacherId != null && !teacherId.isBlank()) {
            sessions = sessionRepository.findByTeacherIdAndIsActiveTrueOrderByStartedAtDesc(teacherId);
        } else {
            sessions = sessionRepository.findByIsActiveTrueOrderByStartedAtDesc();
        }

        // Filter expired ones automatically
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSessionDTO> dtos = new ArrayList<>();
        for (AttendanceSession s : sessions) {
            if (s.getExpiresAt().isBefore(now)) {
                s.setIsActive(false);
                sessionRepository.save(s);
            } else {
                dtos.add(mapToSessionDTO(s));
            }
        }

        return dtos;
    }

    @Transactional
    public AttendanceRecordDTO scanAttendance(ScanAttendanceRequest request) {
        log.info("Student {} scanning QR token: {}", request.getStudentId(), request.getQrToken());

        AttendanceSession session = null;
        if (request.getSessionId() != null) {
            session = sessionRepository.findById(request.getSessionId()).orElse(null);
        }
        if (session == null && request.getQrToken() != null) {
            session = sessionRepository.findByQrTokenAndIsActiveTrue(request.getQrToken().trim()).orElse(null);
        }

        if (session == null) {
            throw new ResourceNotFoundException("Session invalide ou inexistante pour ce code QR");
        }

        if (!Boolean.TRUE.equals(session.getIsActive())) {
            throw new ValidationException("Cette session d'émargement est terminée");
        }

        LocalDateTime now = LocalDateTime.now();
        if (session.getExpiresAt().isBefore(now)) {
            session.setIsActive(false);
            sessionRepository.save(session);
            throw new ValidationException("Ce code QR a expiré");
        }

        // Idempotency: check if student has already scanned for this session
        Optional<AttendanceRecord> existing = recordRepository.findBySessionIdAndStudentId(session.getId(), request.getStudentId());
        if (existing.isPresent()) {
            log.info("Student {} already marked attendance for session {}", request.getStudentId(), session.getId());
            return mapToRecordDTO(existing.get());
        }

        // Resolve student profile
        String studentName = "Étudiant " + request.getStudentId();
        String studentEmail = request.getStudentId() + "@planora.cm";

        Optional<Etudiant> etudiantOpt = etudiantRepository.findById(request.getStudentId());
        if (etudiantOpt.isPresent()) {
            Etudiant e = etudiantOpt.get();
            studentName = (e.getPrenomUser() != null ? e.getPrenomUser() + " " : "") + (e.getNomUser() != null ? e.getNomUser() : "");
            studentEmail = e.getEmail();
        } else {
            Optional<Utilisateur> uOpt = utilisateurRepository.findById(request.getStudentId());
            if (uOpt.isPresent()) {
                Utilisateur u = uOpt.get();
                studentName = (u.getPrenomUser() != null ? u.getPrenomUser() + " " : "") + (u.getNomUser() != null ? u.getNomUser() : "");
                studentEmail = u.getEmail();
            }
        }

        // Determine PRESENT vs LATE: grace period is 15 minutes after session started
        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (now.isAfter(session.getStartedAt().plusMinutes(15))) {
            status = AttendanceStatus.LATE;
        }

        AttendanceRecord record = AttendanceRecord.builder()
                .session(session)
                .studentId(request.getStudentId())
                .studentName(studentName.trim().isEmpty() ? request.getStudentId() : studentName.trim())
                .studentEmail(studentEmail)
                .status(status)
                .scannedAt(now)
                .manualOverride(false)
                .build();

        AttendanceRecord saved = recordRepository.save(record);

        // Auto-enroll student into course if not enrolled
        if (!enrollmentRepository.existsByStudentIdAndCourseId(request.getStudentId(), session.getCourse().getId())) {
            CourseEnrollment enrollment = CourseEnrollment.builder()
                    .studentId(request.getStudentId())
                    .course(session.getCourse())
                    .enrolledAt(now)
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);
        }

        return mapToRecordDTO(saved);
    }

    @Transactional
    public AttendanceRecordDTO overrideAttendance(Long recordId, AttendanceOverrideRequest request, String teacherId) {
        AttendanceRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Enregistrement de présence introuvable: " + recordId));

        record.setStatus(request.getStatus());
        record.setManualOverride(true);
        record.setOverriddenBy(teacherId);

        AttendanceRecord updated = recordRepository.save(record);
        return mapToRecordDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getSessionAttendees(Long sessionId) {
        return recordRepository.findBySessionIdOrderByScannedAtDesc(sessionId)
                .stream()
                .map(this::mapToRecordDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDTO> getStudentHistory(String studentId) {
        return recordRepository.findByStudentIdOrderByScannedAtDesc(studentId)
                .stream()
                .map(this::mapToRecordDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseAttendanceReportDTO getCourseReport(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + courseId));

        List<AttendanceSession> sessions = sessionRepository.findByCourseIdOrderByStartedAtDesc(courseId);
        List<CourseEnrollment> enrollments = enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE);
        List<AttendanceRecord> records = recordRepository.findBySession_Course_Id(courseId);

        long totalSessions = sessions.size();
        long totalEnrolled = enrollments.size();

        Map<String, List<AttendanceRecord>> recordsByStudent = records.stream()
                .collect(Collectors.groupingBy(AttendanceRecord::getStudentId));

        List<CourseAttendanceReportDTO.StudentAttendanceSummaryItem> summaries = new ArrayList<>();
        double totalRateSum = 0;

        for (CourseEnrollment enrollment : enrollments) {
            String stuId = enrollment.getStudentId();
            List<AttendanceRecord> stuRecords = recordsByStudent.getOrDefault(stuId, Collections.emptyList());

            long attended = stuRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
            long late = stuRecords.stream().filter(r -> r.getStatus() == AttendanceStatus.LATE).count();
            long absent = Math.max(0, totalSessions - (attended + late));

            double rate = totalSessions > 0 ? ((attended + late * 0.7) / (double) totalSessions) * 100.0 : 100.0;
            totalRateSum += rate;

            String stuName = stuRecords.isEmpty() ? "Étudiant " + stuId : stuRecords.get(0).getStudentName();
            String stuEmail = stuRecords.isEmpty() ? stuId + "@planora.cm" : stuRecords.get(0).getStudentEmail();

            summaries.add(CourseAttendanceReportDTO.StudentAttendanceSummaryItem.builder()
                    .studentId(stuId)
                    .studentName(stuName)
                    .studentEmail(stuEmail)
                    .sessionsAttended(attended)
                    .sessionsLate(late)
                    .sessionsAbsent(absent)
                    .attendancePercentage(Math.round(rate * 10.0) / 10.0)
                    .status(absent > attended ? "Absent" : (late > attended ? "Late" : "Present"))
                    .build());
        }

        double overallRate = totalEnrolled > 0 ? (totalRateSum / totalEnrolled) : (totalSessions > 0 ? 85.0 : 100.0);

        String teacherName = "Enseignant";
        if (course.getEnseignant() != null) {
            teacherName = (course.getEnseignant().getPrenomUser() != null ? course.getEnseignant().getPrenomUser() + " " : "")
                    + (course.getEnseignant().getNomUser() != null ? course.getEnseignant().getNomUser() : "");
        }

        return CourseAttendanceReportDTO.builder()
                .courseId(course.getId())
                .courseCode(course.getCodeCourse())
                .courseTitle(course.getUe() != null ? course.getUe().getNomUE() : course.getCodeCourse())
                .teacherId(course.getEnseignant() != null ? course.getEnseignant().getIdUser() : "")
                .teacherName(teacherName.trim())
                .totalSessions(totalSessions)
                .totalEnrolledStudents(totalEnrolled)
                .overallAttendanceRate(Math.round(overallRate * 10.0) / 10.0)
                .studentSummaries(summaries)
                .build();
    }

    @Transactional
    public CourseEnrollmentDTO enrollStudent(Long courseId, String studentId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Cours non trouvé avec l'ID: " + courseId));

        Optional<CourseEnrollment> existing = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId);
        if (existing.isPresent()) {
            CourseEnrollment e = existing.get();
            e.setStatus(EnrollmentStatus.ACTIVE);
            return mapToEnrollmentDTO(enrollmentRepository.save(e));
        }

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .studentId(studentId)
                .course(course)
                .enrolledAt(LocalDateTime.now())
                .status(EnrollmentStatus.ACTIVE)
                .build();

        return mapToEnrollmentDTO(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public void unenrollStudent(Long courseId, String studentId) {
        enrollmentRepository.deleteByStudentIdAndCourseId(studentId, courseId);
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDTO> getStudentEnrolledCourses(String studentId) {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentDTO> getCourseEnrolledStudents(Long courseId) {
        return enrollmentRepository.findByCourseIdAndStatus(courseId, EnrollmentStatus.ACTIVE)
                .stream()
                .map(this::mapToEnrollmentDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttendanceSessionDTO> getCourseSessions(Long courseId) {
        return sessionRepository.findByCourseIdOrderByStartedAtDesc(courseId)
                .stream()
                .map(this::mapToSessionDTO)
                .collect(Collectors.toList());
    }

    private AttendanceSessionDTO mapToSessionDTO(AttendanceSession session) {
        long attendeesCount = recordRepository.countBySessionId(session.getId());
        long enrolledCount = enrollmentRepository.countByCourseIdAndStatus(session.getCourse().getId(), EnrollmentStatus.ACTIVE);

        long remainingSec = 0;
        if (session.getExpiresAt() != null && session.getExpiresAt().isAfter(LocalDateTime.now())) {
            remainingSec = Duration.between(LocalDateTime.now(), session.getExpiresAt()).getSeconds();
        }

        String courseTitle = session.getCourse().getCodeCourse();
        if (session.getCourse().getUe() != null && session.getCourse().getUe().getNomUE() != null) {
            courseTitle = session.getCourse().getUe().getNomUE();
        }

        return AttendanceSessionDTO.builder()
                .id(session.getId())
                .courseId(session.getCourse().getId())
                .courseCode(session.getCourse().getCodeCourse())
                .courseTitle(courseTitle)
                .teacherId(session.getTeacherId())
                .teacherName(session.getTeacherName())
                .startedAt(session.getStartedAt())
                .expiresAt(session.getExpiresAt())
                .qrToken(session.getQrToken())
                .isActive(session.getIsActive())
                .room(session.getRoom())
                .sessionType(session.getSessionType())
                .attendeesCount(attendeesCount)
                .enrolledCount(enrolledCount)
                .remainingSeconds(remainingSec)
                .build();
    }

    private AttendanceRecordDTO mapToRecordDTO(AttendanceRecord record) {
        Course c = record.getSession().getCourse();
        String courseTitle = c.getCodeCourse();
        if (c.getUe() != null && c.getUe().getNomUE() != null) {
            courseTitle = c.getUe().getNomUE();
        }

        return AttendanceRecordDTO.builder()
                .id(record.getId())
                .sessionId(record.getSession().getId())
                .courseId(c.getId())
                .courseCode(c.getCodeCourse())
                .courseTitle(courseTitle)
                .studentId(record.getStudentId())
                .studentName(record.getStudentName())
                .studentEmail(record.getStudentEmail())
                .status(record.getStatus())
                .scannedAt(record.getScannedAt())
                .manualOverride(record.getManualOverride())
                .overriddenBy(record.getOverriddenBy())
                .build();
    }

    private CourseEnrollmentDTO mapToEnrollmentDTO(CourseEnrollment enrollment) {
        Course c = enrollment.getCourse();
        String courseTitle = c.getCodeCourse();
        if (c.getUe() != null && c.getUe().getNomUE() != null) {
            courseTitle = c.getUe().getNomUE();
        }

        String teacherName = "Enseignant";
        if (c.getEnseignant() != null) {
            teacherName = (c.getEnseignant().getPrenomUser() != null ? c.getEnseignant().getPrenomUser() + " " : "")
                    + (c.getEnseignant().getNomUser() != null ? c.getEnseignant().getNomUser() : "");
        }

        return CourseEnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudentId())
                .courseId(c.getId())
                .courseCode(c.getCodeCourse())
                .courseTitle(courseTitle)
                .teacherName(teacherName.trim())
                .enrolledAt(enrollment.getEnrolledAt())
                .status(enrollment.getStatus())
                .build();
    }
}

