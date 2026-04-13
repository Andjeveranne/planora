package com.isi4.notificationservice.repository;

import com.isi4.notificationservice.entity.Notification;
import com.isi4.notificationservice.enums.StatutNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByDestinataireIdOrderByDateEnvoiDesc(String destinataireId);

    List<Notification> findByDestinataireIdAndStatutOrderByDateEnvoiDesc(String destinataireId, StatutNotification statut);

    long countByDestinataireIdAndStatut(String destinataireId, StatutNotification statut);
}
