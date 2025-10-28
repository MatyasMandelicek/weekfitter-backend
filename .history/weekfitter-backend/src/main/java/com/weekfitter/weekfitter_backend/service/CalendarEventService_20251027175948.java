package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
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
        if (event.getCategory() == null)
            event.setCategory(ActivityType.OTHER);

        if (event.getTitle() == null || event.getTitle().isBlank())
            throw new RuntimeException("Title is required");

        if (event.getDuration() != null && event.getStartTime() != null)
            event.setEndTime(event.getStartTime().plusMinutes(event.getDuration().longValue()));

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
                    // Zachovat uživatele, pokud nepřišel
                    if (updatedEvent.getUser() == null)
                        updatedEvent.setUser(existing.getUser());

                    // Nastavit výchozí kategorii, pokud chybí
                    if (updatedEvent.getCategory() == null)
                        updatedEvent.setCategory(existing.getCategory() != null ? existing.getCategory() : ActivityType.OTHER);

                    // --- Aktualizace polí ---
                    if (updatedEvent.getTitle() != null)
                        existing.setTitle(updatedEvent.getTitle());
                    if (updatedEvent.getDescription() != null)
                        existing.setDescription(updatedEvent.getDescription());
                    if (updatedEvent.getStartTime() != null)
                        existing.setStartTime(updatedEvent.getStartTime());
                    if (updatedEvent.getEndTime() != null)
                        existing.setEndTime(updatedEvent.getEndTime());
                    if (updatedEvent.getDuration() != null)
                        existing.setDuration(updatedEvent.getDuration());
                    if (updatedEvent.getDistance() != null)
                        existing.setDistance(updatedEvent.getDistance());
                    if (updatedEvent.getSportDescription() != null)
                        existing.setSportDescription(updatedEvent.getSportDescription());
                    if (updatedEvent.getSportType() != null)
                        existing.setSportType(updatedEvent.getSportType());
                    if (updatedEvent.getFilePath() != null)
                        existing.setFilePath(updatedEvent.getFilePath());
                    if (updatedEvent.getCategory() != null)
                        existing.setCategory(updatedEvent.getCategory());
                    existing.setAllDay(updatedEvent.isAllDay());

                    // --- Zachovat notify a notifyBefore ---
                    if (updatedEvent.getNotify() != null)
                        existing.setNotify(updatedEvent.getNotify());
                    if (updatedEvent.getNotifyBefore() != null)
                        existing.setNotifyBefore(updatedEvent.getNotifyBefore());

                    // --- Uživatel ---
                    existing.setUser(updatedEvent.getUser());

                    return calendarEventRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
