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
 * Kromě standardních CRUD operací poskytovaných JpaRepository
 * obsahuje také vlastní dotazy pro:
 * - načítání neodeslaných notifikací, jejichž čas právě nastal,
 * - načítání notifikací podle události nebo uživatele,
 * - hromadné mazání notifikací spojených s událostí.
 */
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    /**
     * Vrací všechny neodeslané notifikace, jejichž čas odeslání již nastal
     * (notifyAt <= now), ale jejich událost ještě nenastala.
     *
     * Logika:
     * - n.sent = false  → notifikace ještě nebyla odeslána
     * - n.notifyAt <= :now  → čas upozornění je teď nebo v minulosti
     * - start události > now nebo je null → událost ještě neproběhla
     *
     * Tato metoda se typicky používá ve "scheduleru", který pravidelně kontroluje,
     * zda má odeslat nové notifikace.
     */
    @Query("SELECT n FROM Notification n WHERE n.sent = false AND n.notifyAt <= :now AND (n.event.startTime IS NULL OR n.event.startTime > :now)")
    List<Notification> findPendingNotifications(LocalDateTime now);

    /**
     * Vrací všechny notifikace patřící dané události podle jejího ID.
     *
     * Spring vytvoří dotaz automaticky podle názvu metody.
     */
    List<Notification> findAllByEventId(UUID eventId);

    /**
     * Vrací všechny notifikace patřící konkrétnímu uživateli.
     *
     * Použitelné např. pro přehled osobních upozornění nebo statistiky.
     */
    List<Notification> findByUserId(UUID userId);
    
    /** Odstraňuje všechny notifikace související s danou událostí. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("delete from Notification n where n.event.id = :eventId")
    int deleteAllByEventId(@Param("eventId") UUID eventId);

}
