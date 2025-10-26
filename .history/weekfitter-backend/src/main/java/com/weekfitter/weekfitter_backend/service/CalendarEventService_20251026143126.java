package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
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

    /**
     * Vrátí všechny události (jen pro administrativní účely, běžně nepoužíváme)
     */
    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    /**
     * Vrátí konkrétní událost podle ID
     */
    public Optional<CalendarEvent> getEventById(UUID id) {
        return calendarEventRepository.findById(id);
    }

    /**
     * Vrátí všechny události podle konkrétního uživatele
     */
    public List<CalendarEvent> getEventsByUser(User user) {
        return calendarEventRepository.findByUser(user);
    }

    /**
     * Vytvoří novou událost (automaticky přiřadí uživatele)
     */
    public CalendarEvent createEvent(CalendarEvent event) {
        if (event.getUser() == null) {
            throw new RuntimeException("Událost musí mít přiřazeného uživatele.");
        }

        if (event.getCategory() == null) {
            event.setCategory(ActivityType.OTHER);
        }

        if (event.getTitle() == null || event.getTitle().isBlank()) {
            throw new RuntimeException("Název události je povinný.");
        }

        if (event.getDuration() != null && event.getStartTime() != null) {
            event.setEndTime(event.getStartTime().plusMinutes(event.getDuration().longValue()));
        }

        return calendarEventRepository.save(event);
    }

    /**
     * ✅ Aktualizuje existující událost
     */
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

                    // DŮLEŽITÉ: necháme přiřazeného uživatele beze změny
                    return calendarEventRepository.save(event);
                })
                .orElseThrow(() -> new RuntimeException("Událost nebyla nalezena."));
    }

    /**
     * Smaže událost
     */
    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
