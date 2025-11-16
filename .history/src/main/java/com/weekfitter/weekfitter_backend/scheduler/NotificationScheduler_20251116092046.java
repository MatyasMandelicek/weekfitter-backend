package com.weekfitter.weekfitter_backend.scheduler;

import com.weekfitter.weekfitter_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler komponenta, která pravidelně kontroluje a odesílá notifikace.
 *
 * Zajišťuje:
 * - jednorázovou kontrolu neodeslaných notifikací po startu aplikace,
 * - periodické spouštění logiky pro odesílání notifikací v pravidelném intervalu.
 *
 * Využívá NotificationService, který provádí samotné vyhledání notifikací
 * v databázi a jejich odeslání (např. e-mail).
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    /** Logger pro záznam průběhu a chyb plánovače. */
    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;

    /** Spuštění kontroly notifikací po startu backendu. */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        log.info("[STARTUP] Kontrola neodeslaných notifikací po startu...");
        try {
            notificationService.sendPendingNotifications();
            log.info("[STARTUP] Kontrola dokončena.");
        } catch (Exception e) {
            log.error("[STARTUP-ERROR] Chyba při kontrole notifikací po startu: {}", e.getMessage(), e);
        }
    }

    /** Kontrola neodeslaných notifikací každých 30 sekund. */
    @Scheduled(fixedRate = 30000)
    public void checkNotifications() {
        try {
            notificationService.sendPendingNotifications();
        } catch (Exception e) {
            log.error("[SCHEDULER-ERROR] Chyba při běhu plánovače notifikací: {}", e.getMessage(), e);
        }
    }
}
