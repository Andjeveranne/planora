package com.isi4.userservice.dto;

import com.isi4.userservice.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceOverrideRequest {
    @NotNull(message = "Le statut est obligatoire")
    private AttendanceStatus status;
}

