package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CalendarEventService {

    private final NotificationRepository notificationRepository;
    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository, NotificationRepository notificationRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.notificationRepository = notificationRepository;
    }

    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public Optional<CalendarEvent> getEventById(UUID id) {
        return calendarEventRepository.findById(id);
    }

    public List<CalendarEvent> getEventsByUser(User user) {
        return calendarEventRepository.findByUser(user);
    }

    public CalendarEvent createEvent(CalendarEvent event) {
        if (event.getCategory() == null) event.setCategory(ActivityType.OTHER);
        if (event.getTitle() == null || event.getTitle().isBlank())
            throw new RuntimeException("Title is required");

        // Nepočítej tady nic složitě – nech to na @PrePersist/@PreUpdate
        // Jen doplň, když uživateli chybí endTime i duration:
        if (event.getStartTime() != null) {
            if ((event.getDuration() == null || event.getDuration() <= 0)
                    && (event.getEndTime() == null || event.getEndTime().isBefore(event.getStartTime()))) {
                event.setEndTime(event.getStartTime().plusHours(1));
            } else if (event.getDuration() != null && event.getDuration() > 0) {
                event.setEndTime(event.getStartTime().plusMinutes(event.getDuration().longValue()));
            }
        }

        return calendarEventRepository.save(event);
    }


    /**
     * Bezpečná aktualizace – zachovává všechny důležité atributy
     * (včetně notify a notifyBefore)
     */
@Transactional
public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
    return calendarEventRepository.findById(id)
            .map(existing -> {
                // 1) zachovat uživatele a kategorii, když nepřijdou
                if (updatedEvent.getUser() == null)
                    updatedEvent.setUser(existing.getUser());
                if (updatedEvent.getCategory() == null)
                    updatedEvent.setCategory(existing.getCategory() != null ? existing.getCategory() : ActivityType.OTHER);

                // 2) běžné copy pouze pokud přišly nové hodnoty
                if (updatedEvent.getTitle() != null) existing.setTitle(updatedEvent.getTitle());
                if (updatedEvent.getDescription() != null) existing.setDescription(updatedEvent.getDescription());
                if (updatedEvent.getStartTime() != null) existing.setStartTime(updatedEvent.getStartTime());
                if (updatedEvent.getDuration() != null) existing.setDuration(updatedEvent.getDuration());
                if (updatedEvent.getDistance() != null) existing.setDistance(updatedEvent.getDistance());
                if (updatedEvent.getSportDescription() != null) existing.setSportDescription(updatedEvent.getSportDescription());
                if (updatedEvent.getSportType() != null) existing.setSportType(updatedEvent.getSportType());
                if (updatedEvent.getFilePath() != null) existing.setFilePath(updatedEvent.getFilePath());
                if (updatedEvent.getCategory() != null) existing.setCategory(updatedEvent.getCategory());
                existing.setAllDay(updatedEvent.isAllDay());
                if (updatedEvent.getNotify() != null) existing.setNotify(updatedEvent.getNotify());
                if (updatedEvent.getNotifyBefore() != null) existing.setNotifyBefore(updatedEvent.getNotifyBefore());
                existing.setUser(updatedEvent.getUser());

                // 3) JEDNOTNÁ LOGIKA KONCE:
                //    - pokud je zadaný startTime:
                //        a) když je >0 duration -> end = start + duration
                //        b) když není duration a nebyl poslán platný endTime -> end = start + 1 hod
                //        c) když klient poslal validní endTime -> respektuj ho
                if (existing.getStartTime() != null) {
                    boolean clientSentEndTime = updatedEvent.getEndTime() != null;
                    if (existing.getDuration() != null && existing.getDuration() > 0) {
                        existing.setEndTime(existing.getStartTime().plusMinutes(existing.getDuration().longValue()));
                    } else if (!clientSentEndTime || updatedEvent.getEndTime().isBefore(existing.getStartTime())) {
                        existing.setEndTime(existing.getStartTime().plusHours(1));
                    } else {
                        existing.setEndTime(updatedEvent.getEndTime());
                    }
                } else if (updatedEvent.getEndTime() != null) {
                    // fallback: když není startTime, ale přišel endTime
                    existing.setEndTime(updatedEvent.getEndTime());
                }

                return calendarEventRepository.save(existing);
            })
            .orElseThrow(() -> new RuntimeException("Event not found"));
}


    @Transactional
    public void deleteEvent(UUID id) {
        notificationRepository.deleteAllByEventId(id);
        calendarEventRepository.deleteById(id);
    }
}
