package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.Notification;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

@Query("SELECT n FROM Notification n WHERE n.sent = false AND n.notifyAt <= :now AND n.event.startTime > :now")
List<Notification> findPendingNotifications(LocalDateTime now);


    List<Notification> findByUserId(UUID userId);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.event.id = :eventId")
    int deleteAllByEventId(@Param("eventId") UUID eventId);

}
