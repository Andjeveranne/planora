package com.isi4.planningservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "generation_runs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "school_id")
    private String schoolId;

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "speciality_id")
    private String specialityId;

    @Column(name = "level_id")
    private String levelId;

    @Column(name = "generated_by")
    private String generatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GenerationRunStatus status = GenerationRunStatus.PENDING;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }
    }
}
