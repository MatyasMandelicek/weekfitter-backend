package com.weekfitter.weekfitter_backend.scheduler;

import com.weekfitter.weekfitter_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler komponenta, která pravidelně kontroluje neodeslané notifikace
 * a spouští jejich odeslání pomocí {@link NotificationService}.
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    
    /**
     * Metoda se spouští automaticky každou minutu a kontroluje,
     * zda nenastal čas odeslat některou z neodeslaných notifikací.
     */
    @Scheduled(fixedRate = 30000) // každých 30 sekund
    public void checkNotifications() {
        notificationService.sendPendingNotifications();
    }
}
