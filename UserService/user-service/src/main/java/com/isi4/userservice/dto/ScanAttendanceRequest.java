package com.isi4.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanAttendanceRequest {
    private Long sessionId;

    @NotBlank(message = "Le token QR est obligatoire")
    private String qrToken;

    @NotBlank(message = "L'identifiant de l'étudiant est obligatoire")
    private String studentId;
}

