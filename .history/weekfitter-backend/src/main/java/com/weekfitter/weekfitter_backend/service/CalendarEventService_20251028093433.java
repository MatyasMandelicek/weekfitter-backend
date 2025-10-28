package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final NotificationRepository notificationRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final NotificationService notificationService; // <-- přidáno

    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public Optional<CalendarEvent> getEventById(UUID id) {
        return calendarEventRepository.findById(id);
    }

    public List<CalendarEvent> getEventsByUser(User user) {
        return calendarEventRepository.findByUser(user);
    }

    /**
     * Vytvoří novou událost a nastaví výchozí konec,
     * pokud není zadán nebo je kratší než začátek.
     * Zároveň umožňuje zpracování notifikací (přes controller).
     */
    @Transactional
    public CalendarEvent createEvent(CalendarEvent event) {
        if (event.getCategory() == null) event.setCategory(ActivityType.OTHER);
        if (event.getTitle() == null || event.getTitle().isBlank())
            throw new RuntimeException("Title is required");

        // Výpočet endTime
        if (event.getStartTime() != null) {
            if ((event.getDuration() == null || event.getDuration() <= 0)
                    && (event.getEndTime() == null || event.getEndTime().isBefore(event.getStartTime()))) {
                event.setEndTime(event.getStartTime().plusHours(1));
            } else if (event.getDuration() != null && event.getDuration() > 0) {
                event.setEndTime(event.getStartTime().plusMinutes(event.getDuration().longValue()));
            }
        }

        CalendarEvent saved = calendarEventRepository.save(event);
        return saved;
    }

    /**
     * Aktualizace existující události.
     * Zachovává původního uživatele a přepočítává čas konce.
     */
    @Transactional
    public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
        return calendarEventRepository.findById(id)
                .map(existing -> {
                    if (updatedEvent.getUser() == null)
                        updatedEvent.setUser(existing.getUser());
                    if (updatedEvent.getCategory() == null)
                        updatedEvent.setCategory(existing.getCategory() != null ? existing.getCategory() : ActivityType.OTHER);

                    // === Přepis jednotlivých polí ===
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

                    // === Přepočet konce ===
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
                        existing.setEndTime(updatedEvent.getEndTime());
                    }

                    return calendarEventRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    /**
     * Smazání události i s navázanými notifikacemi
     */
    @Transactional
    public void deleteEvent(UUID id) {
        notificationRepository.deleteAllByEventId(id);
        calendarEventRepository.deleteById(id);
    }

    // ==============================================================
    // Doplňkové metody pro vytváření více notifikací z controlleru
    // ==============================================================

    /**
     * Po uložení události vytvoří notifikace pro dané minuty předem.
     * @param event uložená událost
     * @param notificationMinutes seznam minut (např. [15, 60, 1440])
     */
    @Transactional
    public void createNotificationsForEvent(CalendarEvent event, List<Integer> notificationMinutes) {
        if (event == null || event.getStartTime() == null) return;

        // Smaž staré notifikace
        notificationRepository.deleteAllByEventId(event.getId());

        if (notificationMinutes == null || notificationMinutes.isEmpty()) return;

        for (Integer minutes : notificationMinutes) {
            if (minutes != null && minutes > 0) {
                LocalDateTime notifyAt = event.getStartTime().minusMinutes(minutes.longValue());
                notificationService.createNotification(event, notifyAt);
            }
        }
    }
}
