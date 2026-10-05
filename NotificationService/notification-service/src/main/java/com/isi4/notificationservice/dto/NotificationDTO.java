package com.isi4.notificationservice.dto;

import com.isi4.notificationservice.enums.StatutNotification;
import com.isi4.notificationservice.enums.TypeNotif;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;

    @NotBlank(message = "L'identifiant du destinataire est obligatoire")
    private String destinataireId;

    @NotNull(message = "Le canal de notification est obligatoire")
    private TypeNotif canal;

    @NotBlank(message = "Le sujet est obligatoire")
    private String sujet;

    @NotBlank(message = "Le message est obligatoire")
    private String message;

    private StatutNotification statut;
    private LocalDateTime dateEnvoi;
    private String destinataireEmail;
    private LocalDateTime scheduledAt;
    private String deduplicationKey;
}
