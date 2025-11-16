package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.Notification;
import com.weekfitter.weekfitter_backend.model.NotificationType;
import com.weekfitter.weekfitter_backend.repository.NotificationRepository;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.*;

/**
 * Service třída zajišťující správu notifikací pro uživatelské události.
 *
 * Odpovídá za:
 * - tvorbu notifikací pro konkrétní události,
 * - přepočet časů notifikací při změně začátku události,
 * - periodické vyhledávání a odesílání e-mailových upozornění.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    /** Časová zóna používaná aplikací (Evropa – Praha). */
    private static final ZoneId APP_ZONE = ZoneId.of("Europe/Prague");

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Vytvoří novou notifikaci k dané události.
     *
     * Notifikace je navázána:
     * - na konkrétní CalendarEvent,
     * - na konkrétního uživatele (vlastníka události).
     *
     * @param event    událost, ke které notifikace patří
     * @param notifyAt datum a čas, kdy má být upozornění odesláno
     * @param type     typ notifikace (offset v minutách před začátkem)
     */
    public void createNotification(CalendarEvent event, LocalDateTime notifyAt, NotificationType type) {
        // Pokud událost nemá přiřazeného uživatele, nedává smysl notifikaci vytvářet
        if (event.getUser() == null) return;

        Notification notification = Notification.builder()
                .event(event)
                .user(event.getUser())
                .notifyAt(notifyAt)
                .notificationType(type)
                .sent(false)
                .build();

        notificationRepository.save(notification);
        log.info("Vytvořena notifikace typu {} pro uživatele {} (odeslání v {})",
                type, event.getUser().getEmail(), notifyAt);
    }


    /**
     * Odstraní všechny notifikace navázané na konkrétní událost.
     *
     * Používá se např. při smazání události nebo jejím kompletním přegenerování.
     *
     * @param eventId ID události
     */
    @Transactional
    public void deleteByEvent(UUID eventId) {
        notificationRepository.deleteAllByEventId(eventId);
        log.info("Odstraněny notifikace pro událost ID {}", eventId);
    }

    /**
     * Přepočítá časy všech stávajících notifikací, pokud došlo k posunutí začátku události.
     *
     * Logika:
     * - pro každou notifikaci vypočte, kolik minut před starým startem byla naplánována,
     * - stejný offset pak použije vůči novému startu.
     *
     * @param eventId  ID události, které se změnil začátek
     * @param oldStart původní čas začátku
     * @param newStart nový čas začátku
     */
    @Transactional
    public void rebaseExistingNotificationsToNewStart(
            UUID eventId,
            LocalDateTime oldStart,
            LocalDateTime newStart
    ) {
        List<Notification> existing = notificationRepository.findAllByEventId(eventId);
        if (existing == null || existing.isEmpty()) return;

        for (Notification n : existing) {
            if (n.getNotifyAt() == null) continue;

            // Vypočítá, kolik minut byla notifikace před původním začátkem
            long minutesBeforeOldStart = Math.abs(Duration.between(n.getNotifyAt(), oldStart).toMinutes());

            // Stejný offset použijeme i vůči novému začátku
            LocalDateTime newNotifyAt = newStart.minusMinutes(minutesBeforeOldStart);
            n.setNotifyAt(newNotifyAt);
        }

        notificationRepository.saveAll(existing);
        log.info("Přepočítány časy notifikací pro událost ID {}", eventId);
    }

    /**
     * Odesílá všechny notifikace, jejichž čas právě nastal (notifyAt <= teď)
     * a zároveň ještě neproběhla související událost.
     *
     * Metoda je spouštěna periodicky pomocí NotificationScheduleru.
     */
    @Transactional
    public void sendPendingNotifications() {
        // Aktuální čas v časové zóně aplikace (Evropa/Prague)
        LocalDateTime now = ZonedDateTime.now(APP_ZONE).toLocalDateTime();

        List<Notification> pending = notificationRepository.findPendingNotifications(now);
        if (pending.isEmpty()) {
            return;
        }

        // Formát pro zobrazení data a času v e-mailu (česky)
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("d. MMMM yyyy 'v' HH:mm", new Locale("cs"));

        for (Notification n : pending) {
            try {
                if (n.getUser() == null || n.getEvent() == null) continue;

                CalendarEvent e = n.getEvent();

                // Bezpečnostní kontrola: pokud už událost proběhla, nemá smysl posílat upozornění
                if (e.getStartTime() != null && e.getStartTime().isBefore(now)) {
                    n.setSent(true);
                    notificationRepository.save(n);
                    continue;
                }

                String subject = "Upozornění: " + e.getTitle();
                String message =
                        "Připomenutí vaší aktivity:\n\n" +
                        "Název: " + e.getTitle() + "\n" +
                        "Popis: " + (e.getDescription() != null ? e.getDescription() : "—") + "\n" +
                        "Kategorie: " + e.getCategory() + "\n" +
                        (e.getSportType() != null ? "Typ sportu: " + e.getSportType() + "\n" : "") +
                        (e.getDistance() != null ? "Vzdálenost: " + e.getDistance() + " km\n" : "") +
                        (e.getDuration() != null ? "Doba trvání: " + e.getDuration() + " min\n" : "") +
                        "\nZačíná: " + e.getStartTime().format(formatter);
                // Odeslání e-mailu přes EmailService

                emailService.sendNotificationEmail(n.getUser().getEmail(), subject, message);
                n.setSent(true);
                notificationRepository.save(n);

                log.info("Notifikace odeslána uživateli {} pro událost '{}'",
                        n.getUser().getEmail(), e.getTitle());

            } catch (Exception ex) {
                log.error("Chyba při odesílání notifikace: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Vrátí seznam offsetů (v minutách před začátkem události),
     * podle aktuálních notifikací v databázi.
     */
    public List<Integer> getNotificationOffsetsForEvent(CalendarEvent event) {
        if (event == null || event.getId() == null || event.getStartTime() == null) {
            return Collections.emptyList();
        }

        List<Notification> notifications = notificationRepository.findAllByEventId(event.getId());
        if (notifications == null || notifications.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> offsets = new ArrayList<>();

        for (Notification n : notifications) {
            if (n.getNotifyAt() == null) continue;

            long minutes = Duration.between(n.getNotifyAt(), event.getStartTime()).toMinutes();
            // měly by být kladné – notifikace před začátkem
            if (minutes > 0) {
                offsets.add((int) minutes);
            }
        }

        // pro přehlednost seřadíme od nejkratšího k nejdelšímu
        Collections.sort(offsets);
        return offsets;
    }

}
