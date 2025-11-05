package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.notifyAt <= :now AND n.event.startTime > :now")
    List<Notification> findPendingNotifications(LocalDateTime now);
    List<Notification> findAllByEventId(UUID eventId);
    List<Notification> findByUserId(UUID userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.event.id = :eventId")
    int deleteAllByEventId(@Param("eventId") UUID eventId);

}
