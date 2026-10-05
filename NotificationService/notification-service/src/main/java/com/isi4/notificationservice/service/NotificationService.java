package com.isi4.notificationservice.service;

import com.isi4.notificationservice.dto.NotificationDTO;
import com.isi4.notificationservice.entity.Notification;
import com.isi4.notificationservice.enums.StatutNotification;
import com.isi4.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.isi4.notificationservice.enums.TypeNotif;
import java.time.LocalDateTime;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public NotificationDTO envoyer(NotificationDTO dto) {
        log.info("Envoi notification canal={} a destinataire={}", dto.getCanal(), dto.getDestinataireId());
        if (dto.getDeduplicationKey() != null && !dto.getDeduplicationKey().isBlank()) {
            var existing = notificationRepository.findByDeduplicationKey(dto.getDeduplicationKey());
            if (existing.isPresent()) return toDTO(existing.get());
        }
        Notification notification = new Notification();
        notification.setDestinataireId(dto.getDestinataireId());
        notification.setCanal(dto.getCanal());
        notification.setSujet(dto.getSujet());
        notification.setMessage(dto.getMessage());
        notification.setDestinataireEmail(dto.getDestinataireEmail());
        notification.setScheduledAt(dto.getScheduledAt());
        notification.setDeduplicationKey(dto.getDeduplicationKey());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification creee avec ID={}", saved.getId());
        return toDTO(saved);
    }

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void deliverScheduledEmails() {
        notificationRepository.findAll().stream()
                .filter(n -> n.getCanal() == TypeNotif.EMAIL)
                .filter(n -> n.getSentAt() == null)
                .filter(n -> n.getScheduledAt() == null || !n.getScheduledAt().isAfter(LocalDateTime.now()))
                .filter(n -> n.getDestinataireEmail() != null && !n.getDestinataireEmail().isBlank())
                .forEach(notification -> {
                    try {
                        SimpleMailMessage mail = new SimpleMailMessage();
                        mail.setTo(notification.getDestinataireEmail());
                        mail.setSubject(notification.getSujet());
                        mail.setText(notification.getMessage());
                        mailSender.send(mail);
                        notification.setSentAt(LocalDateTime.now());
                        notificationRepository.save(notification);
                    } catch (RuntimeException e) {
                        log.warn("Rappel email {} non envoyé; il sera réessayé: {}", notification.getId(), e.getMessage());
                    }
                });
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getByDestinataire(String destinataireId) {
        return notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(destinataireId)
                .stream().filter(this::isVisibleNow)
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNonLues(String destinataireId) {
        return notificationRepository
                .findByDestinataireIdAndStatutOrderByDateEnvoiDesc(destinataireId, StatutNotification.NON_LU)
            .stream().filter(this::isVisibleNow).map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countNonLues(String destinataireId) {
        return getNonLues(destinataireId).size();
    }

    @Transactional
    public NotificationDTO marquerCommeLu(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification introuvable avec l'ID: " + id));
        notification.setStatut(StatutNotification.LU);
        return toDTO(notificationRepository.save(notification));
    }

    @Transactional
    public void marquerToutesCommeLues(String destinataireId) {
        List<Notification> nonLues = notificationRepository
            .findByDestinataireIdAndStatutOrderByDateEnvoiDesc(destinataireId, StatutNotification.NON_LU)
            .stream().filter(this::isVisibleNow).collect(Collectors.toList());
        nonLues.forEach(n -> n.setStatut(StatutNotification.LU));
        notificationRepository.saveAll(nonLues);
        log.info("{} notifications marquees comme lues pour {}", nonLues.size(), destinataireId);
    }

    @Transactional
    public void supprimer(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification introuvable avec l'ID: " + id);
        }
        notificationRepository.deleteById(id);
    }

    private NotificationDTO toDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setDestinataireId(n.getDestinataireId());
        dto.setCanal(n.getCanal());
        dto.setSujet(n.getSujet());
        dto.setMessage(n.getMessage());
        dto.setStatut(n.getStatut());
        dto.setDateEnvoi(n.getDateEnvoi());
        dto.setDestinataireEmail(n.getDestinataireEmail());
        dto.setScheduledAt(n.getScheduledAt());
        dto.setDeduplicationKey(n.getDeduplicationKey());
        return dto;
    }

    private boolean isVisibleNow(Notification notification) {
        return notification.getScheduledAt() == null || !notification.getScheduledAt().isAfter(LocalDateTime.now());
    }
}
