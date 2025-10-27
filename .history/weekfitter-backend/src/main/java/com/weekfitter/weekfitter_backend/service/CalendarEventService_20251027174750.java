package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import org.springframework.stereotype.Service;

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
        if (event.getCategory() == null) event.setCategory(ActivityType.OTHER);
        if (event.getTitle() == null || event.getTitle().isBlank())
            throw new RuntimeException("Title is required");

        if (event.getDuration() != null && event.getStartTime() != null) {
            event.setEndTime(event.getStartTime().plusMinutes(event.getDuration().longValue()));
        }

        return calendarEventRepository.save(event);
    }

    /**
     * Bezpečná aktualizace – zachovává uživatele a typ aktivity
     */
    public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
        return calendarEventRepository.findById(id)
                .map(existing -> {
                    // === zachování uživatele, pokud nepřišel ===
                    if (updatedEvent.getUser() == null) {
                        updatedEvent.setUser(existing.getUser());
                    }

                    // === nastavení typu aktivity ===
                    if (updatedEvent.getCategory() == null) {
                        updatedEvent.setCategory(existing.getCategory() != null ? existing.getCategory() : ActivityType.OTHER);
                    }

                    // === aktualizace polí ===
                    existing.setTitle(updatedEvent.getTitle());
                    existing.setDescription(updatedEvent.getDescription());
                    existing.setStartTime(updatedEvent.getStartTime());

                    if (updatedEvent.getDuration() != null && updatedEvent.getStartTime() != null) {
                        existing.setEndTime(updatedEvent.getStartTime().plusMinutes(updatedEvent.getDuration().longValue()));
                    } else {
                        existing.setEndTime(updatedEvent.getEndTime());
                    }

                    existing.setCategory(updatedEvent.getCategory());
                    existing.setAllDay(updatedEvent.isAllDay());
                    existing.setDuration(updatedEvent.getDuration());
                    existing.setDistance(updatedEvent.getDistance());
                    existing.setSportDescription(updatedEvent.getSportDescription());
                    existing.setSportType(updatedEvent.getSportType());
                    existing.setFilePath(updatedEvent.getFilePath());
                    existing.setUser(updatedEvent.getUser());

                    return calendarEventRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
