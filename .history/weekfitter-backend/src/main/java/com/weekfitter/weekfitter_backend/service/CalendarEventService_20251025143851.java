package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;
    private final UserRepository userRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository, UserRepository userRepository) {
        this.calendarEventRepository = calendarEventRepository;
        this.userRepository = userRepository;
    }

    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public Optional<CalendarEvent> getEventById(UUID id) {
        return calendarEventRepository.findById(id);
    }

    public List<CalendarEvent> getEventsByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return calendarEventRepository.findByUser(user);
    }

public CalendarEvent createEvent(CalendarEvent event) {
    if (event.getCategory() == null) event.setCategory(ActivityType.OTHER);
    return calendarEventRepository.save(event);
}

    public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
        return calendarEventRepository.findById(id)
                .map(event -> {
                    event.setTitle(updatedEvent.getTitle());
                    event.setDescription(updatedEvent.getDescription());
                    event.setStartTime(updatedEvent.getStartTime());
                    event.setEndTime(updatedEvent.getEndTime());
                    event.setCategory(updatedEvent.getCategory());
                    event.setUser(updatedEvent.getUser());
                    return calendarEventRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
