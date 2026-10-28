package com.isi4.planningservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerationRequestDto {
    private String schoolId;
    private String departmentId;
    private String specialityId;
    private String levelId;
    private String week;
    private String generatedBy;
}
