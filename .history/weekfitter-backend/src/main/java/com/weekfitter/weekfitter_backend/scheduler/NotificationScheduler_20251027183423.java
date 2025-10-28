package com.weekfitter.weekfitter_backend.scheduler;

import com.weekfitter.weekfitter_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    // Kontrola každých 30 sekund
    @Scheduled(fixedRate = 60000)
    public void checkNotifications() {
        notificationService.sendPendingNotifications();
    }
}
