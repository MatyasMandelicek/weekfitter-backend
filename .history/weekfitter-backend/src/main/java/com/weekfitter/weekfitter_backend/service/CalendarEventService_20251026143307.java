package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.model.ActivityType;
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

    // nová verze pro načtení podle uživatele (už nepotřebuje userRepository)
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

    public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
        return calendarEventRepository.findById(id)
                .map(event -> {
                    event.setTitle(updatedEvent.getTitle());
                    event.setDescription(updatedEvent.getDescription());
                    event.setStartTime(updatedEvent.getStartTime());

                    if (updatedEvent.getDuration() != null && updatedEvent.getStartTime() != null) {
                        event.setEndTime(updatedEvent.getStartTime().plusMinutes(updatedEvent.getDuration().longValue()));
                    } else {
                        event.setEndTime(updatedEvent.getEndTime());
                    }

                    event.setCategory(updatedEvent.getCategory() != null ? updatedEvent.getCategory() : ActivityType.OTHER);
                    event.setAllDay(updatedEvent.isAllDay());
                    event.setDuration(updatedEvent.getDuration());
                    event.setDistance(updatedEvent.getDistance());
                    event.setSportDescription(updatedEvent.getSportDescription());
                    event.setSportType(updatedEvent.getSportType());
                    event.setFilePath(updatedEvent.getFilePath());
                    event.setUser(updatedEvent.getUser());

                    return calendarEventRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
