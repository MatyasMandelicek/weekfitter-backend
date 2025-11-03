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

    @Transactional
    public void updateNotificationTimesForEvent(CalendarEvent event) {
        if (event == null || event.getId() == null || event.getStartTime() == null) return;

        List<Notification> existing = notificationRepository.findAllByEventId(event.getId());
        if (existing == null || existing.isEmpty()) return;

        // Zjisti rozdíl mezi novým a starým start time
        LocalDateTime newStart = event.getStartTime();
        LocalDateTime oldStart = null;

        // Načti původní událost z DB, abychom věděli, o kolik se posunula
        if (event.getId() != null) {
            CalendarEvent oldEvent = calendarEventRepository.findById(event.getId()).orElse(null);
            if (oldEvent != null) {
                oldStart = oldEvent.getStartTime();
            }
        }

        if (oldStart == null) return;

        long shiftMinutes = java.time.Duration.between(oldStart, newStart).toMinutes();

        for (Notification n : existing) {
            LocalDateTime newNotifyAt = n.getNotifyAt().plusMinutes(shiftMinutes);
            n.setNotifyAt(newNotifyAt);
        }

        notificationRepository.saveAll(existing);
    }


}
