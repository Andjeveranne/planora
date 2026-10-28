package com.isi4.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartSessionRequest {
    @NotNull(message = "L'identifiant du cours est obligatoire")
    private Long courseId;
    
    @Builder.Default
    private Integer durationMinutes = 90;
    
    private String room;
    private String sessionType;
}

