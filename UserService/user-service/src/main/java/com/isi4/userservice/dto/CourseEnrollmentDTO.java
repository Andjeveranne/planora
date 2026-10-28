package com.isi4.userservice.dto;

import com.isi4.userservice.model.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentDTO {
    private Long id;
    private String studentId;
    private Long courseId;
    private String courseCode;
    private String courseTitle;
    private String teacherName;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
}

