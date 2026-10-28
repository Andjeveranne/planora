package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationSummaryDto {
    private Long generationRunId;
    private String schoolId;
    private String departmentId;
    private String specialityId;
    private String levelId;
    private String status;
    private int plannedSessions;
    private int conflicts;
}
