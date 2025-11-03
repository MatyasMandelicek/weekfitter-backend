package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.Notification;
import com.weekfitter.weekfitter_backend.repository.NotificationRepository;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    /**
     * Vytvoří notifikaci k události.
     */
    public void createNotification(CalendarEvent event, LocalDateTime notifyAt) {
        if (event.getUser() == null) return;

        Notification notification = Notification.builder()
                .event(event)
                .user(event.getUser())
                .notifyAt(notifyAt)
                .sent(false)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void deleteByEvent(UUID eventId) {
        notificationRepository.deleteAllByEventId(eventId);
    }

    
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


            long minutesBeforeOldStart = Duration.between(n.getNotifyAt(), oldStart).toMinutes();
            if (minutesBeforeOldStart < 0) {
                minutesBeforeOldStart = -minutesBeforeOldStart;
            }


            LocalDateTime newNotifyAt = newStart.minusMinutes(minutesBeforeOldStart);
            n.setNotifyAt(newNotifyAt);
        }

        notificationRepository.saveAll(existing);
    }

    /**
     * Odesílá všechny neodeslané notifikace, jejichž čas notifyAt již nastal.
     */
    @Transactional
    public void sendPendingNotifications() {
        List<Notification> pending = notificationRepository.findPendingNotifications(LocalDateTime.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. MMMM yyyy 'v' HH:mm", new Locale("cs"));

        for (Notification n : pending) {
            if (n.getUser() != null && n.getEvent() != null) {

                CalendarEvent e = n.getEvent();

                if (e.getStartTime() != null && e.getStartTime().isBefore(LocalDateTime.now())) {
                    n.setSent(true); // označíme jako odeslanou, aby se už nikdy neposlala
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

                emailService.sendNotificationEmail(n.getUser().getEmail(), subject, message);
                n.setSent(true);
                notificationRepository.save(n);

                System.out.println("[INFO] Odesláno upozornění pro \"" + e.getTitle() +
                        "\" uživateli " + n.getUser().getEmail() +
                        " (" + e.getStartTime().format(formatter) + ")");
            }
        }
    }
}
