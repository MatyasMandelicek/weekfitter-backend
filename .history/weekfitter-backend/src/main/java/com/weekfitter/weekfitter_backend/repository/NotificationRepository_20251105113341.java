package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository rozhraní pro práci s entitou Notification.
 * 
 * Definuje vlastní dotazy pro načítání neodeslaných notifikací
 * a mazání všech upozornění spojených s konkrétní událostí.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Vrací seznam všech neodeslaných notifikací, jejichž čas již nastal
     * a jejichž událost je stále v budoucnosti.
     */
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.notifyAt <= :now AND (n.event.startTime IS NUllL OR n.event.startTime > :now")
    List<Notification> findPendingNotifications(LocalDateTime now);

    /** Vrací všechny notifikace podle ID události. */
    List<Notification> findAllByEventId(UUID eventId);

    /** Vrací všechny notifikace patřící uživateli. */
    List<Notification> findByUserId(UUID userId);
    
    /** Odstraňuje všechny notifikace související s danou událostí. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.event.id = :eventId")
    int deleteAllByEventId(@Param("eventId") UUID eventId);

}
