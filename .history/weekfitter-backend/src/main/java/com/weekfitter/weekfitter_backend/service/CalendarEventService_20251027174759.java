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
    // CalendarEventService.java
    @Transactional
    public CalendarEvent updateEvent(UUID id, CalendarEvent patch) {
        CalendarEvent existing = calendarEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Událost " + id + " nenalezena."));

        // pouze pole, která mají smysl editovat (ostatní necháváš)
        if (patch.getTitle() != null) existing.setTitle(patch.getTitle());
        if (patch.getDescription() != null) existing.setDescription(patch.getDescription());
        if (patch.getStartTime() != null) existing.setStartTime(patch.getStartTime());
        if (patch.getEndTime() != null) existing.setEndTime(patch.getEndTime());
        if (patch.getCategory() != null) existing.setCategory(patch.getCategory());
        existing.setAllDay(patch.isAllDay()); // pokud posíláš vždy (jinak použij wrapper + null check)

        if (patch.getDuration() != null) existing.setDuration(patch.getDuration());
        if (patch.getDistance() != null) existing.setDistance(patch.getDistance());
        if (patch.getSportDescription() != null) existing.setSportDescription(patch.getSportDescription());
        if (patch.getSportType() != null) existing.setSportType(patch.getSportType());
        if (patch.getFilePath() != null) existing.setFilePath(patch.getFilePath());

        // 🔴 DŮLEŽITÉ: notifikační pole NIKDY nepřepisuj na null
        if (patch.getNotify() != null) existing.setNotify(patch.getNotify());
        if (patch.getNotifyBefore() != null) existing.setNotifyBefore(patch.getNotifyBefore());

        // uživatele také jen pokud přišel
        if (patch.getUser() != null) existing.setUser(patch.getUser());

        return calendarEventRepository.save(existing);
    }


    public void deleteEvent(UUID id) {
        calendarEventRepository.deleteById(id);
    }
}
