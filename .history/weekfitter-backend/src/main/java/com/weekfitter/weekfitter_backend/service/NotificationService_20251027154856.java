package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.Notification;
import com.weekfitter.weekfitter_backend.respository.NotificationRepository;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

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


    public void createCustomNotification(UUID userId, String title, String message, LocalDateTime notifyAt) {
        Notification notification = Notification.builder()
                .customNotification(true)
                .customTitle(title)
                .customMessage(message)
                .notifyAt(notifyAt)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional
    public void sendPendingNotifications() {
        List<Notification> pending = notificationRepository.findPendingNotifications(LocalDateTime.now());
        for (Notification n : pending) {
            if (n.getUser() != null) {
                if (n.isCustomNotification()) {
                    emailService.sendNotificationEmail(
                            n.getUser().getEmail(),
                            n.getCustomTitle(),
                            n.getCustomMessage()
                    );
                } else if (n.getEvent() != null) {
                    CalendarEvent e = n.getEvent();
                    String subject = "Upozornění: " + e.getTitle();
                    String message = "Připomenutí vaší aktivity:\n\n" +
                            "Název: " + e.getTitle() + "\n" +
                            "Popis: " + (e.getDescription() != null ? e.getDescription() : "—") + "\n" +
                            "Kategorie: " + e.getCategory() + "\n" +
                            (e.getSportType() != null ? "Typ sportu: " + e.getSportType() + "\n" : "") +
                            (e.getDistance() != null ? "Vzdálenost: " + e.getDistance() + " km\n" : "") +
                            (e.getDuration() != null ? "Doba trvání: " + e.getDuration() + " min\n" : "") +
                            "\nZačněte v: " + e.getStartTime();

                    emailService.sendNotificationEmail(
                            n.getUser().getEmail(),
                            subject,
                            message
                    );
                }
                n.setSent(true);
                notificationRepository.save(n);
            }
        }
    }
}
