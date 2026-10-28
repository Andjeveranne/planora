package com.isi4.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceSessionDTO {
    private Long id;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String teacherId;
    private String teacherName;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private String qrToken;
    private Boolean isActive;
    private String room;
    private String sessionType;
    private long attendeesCount;
    private long enrolledCount;
    private long remainingSeconds;
}

