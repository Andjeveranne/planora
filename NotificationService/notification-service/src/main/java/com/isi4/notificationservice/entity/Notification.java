package com.isi4.notificationservice.entity;

import com.isi4.notificationservice.enums.StatutNotification;
import com.isi4.notificationservice.enums.TypeNotif;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destinataire_id", nullable = false)
    private String destinataireId;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false)
    private TypeNotif canal;

    @Column(name = "sujet", nullable = false)
    private String sujet;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutNotification statut = StatutNotification.NON_LU;

    @Column(name = "date_envoi", nullable = false)
    private LocalDateTime dateEnvoi;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "destinataire_email")
    private String destinataireEmail;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "deduplication_key", unique = true, length = 180)
    private String deduplicationKey;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (dateEnvoi == null) dateEnvoi = LocalDateTime.now();
        if (statut == null) statut = StatutNotification.NON_LU;
    }
}
