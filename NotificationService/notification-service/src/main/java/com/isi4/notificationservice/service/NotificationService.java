package com.isi4.notificationservice.service;

import com.isi4.notificationservice.dto.NotificationDTO;
import com.isi4.notificationservice.entity.Notification;
import com.isi4.notificationservice.enums.StatutNotification;
import com.isi4.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDTO envoyer(NotificationDTO dto) {
        log.info("Envoi notification canal={} a destinataire={}", dto.getCanal(), dto.getDestinataireId());
        Notification notification = new Notification();
        notification.setDestinataireId(dto.getDestinataireId());
        notification.setCanal(dto.getCanal());
        notification.setSujet(dto.getSujet());
        notification.setMessage(dto.getMessage());
        Notification saved = notificationRepository.save(notification);
        log.info("Notification creee avec ID={}", saved.getId());
        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getByDestinataire(String destinataireId) {
        return notificationRepository.findByDestinataireIdOrderByDateEnvoiDesc(destinataireId)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getNonLues(String destinataireId) {
        return notificationRepository
                .findByDestinataireIdAndStatutOrderByDateEnvoiDesc(destinataireId, StatutNotification.NON_LU)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long countNonLues(String destinataireId) {
        return notificationRepository.countByDestinataireIdAndStatut(destinataireId, StatutNotification.NON_LU);
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
                .findByDestinataireIdAndStatutOrderByDateEnvoiDesc(destinataireId, StatutNotification.NON_LU);
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
        return dto;
    }
}
