package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseAttendanceReportDTO {
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String teacherId;
    private String teacherName;
    private long totalSessions;
    private long totalEnrolledStudents;
    private double overallAttendanceRate;
    private List<StudentAttendanceSummaryItem> studentSummaries;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentAttendanceSummaryItem {
        private String studentId;
        private String studentName;
        private String studentEmail;
        private long sessionsAttended;
        private long sessionsLate;
        private long sessionsAbsent;
        private double attendancePercentage;
        private String status; // Present / Absent / Late
    }
}

