package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.notifyAt <= :now")
    List<Notification> findPendingNotifications(LocalDateTime now);

    List<Notification> findByUserId(UUID userId);
}
