package com.isi4.userservice.dto;

import com.isi4.userservice.model.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRecordDTO {
    private Long id;
    private Long sessionId;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String studentId;
    private String studentName;
    private String studentEmail;
    private AttendanceStatus status;
    private LocalDateTime scannedAt;
    private Boolean manualOverride;
    private String overriddenBy;
}

