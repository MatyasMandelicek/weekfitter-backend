package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servisní vrstva pro práci s událostmi kalendáře.
 *
 * Poskytuje aplikační logiku pro CRUD operace nad entitou CalendarEvent
 * a doplňuje ji o výpočet konce události, pokud není explicitně zadán.
 */
@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final NotificationRepository notificationRepository;
    private final CalendarEventRepository calendarEventRepository;

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
     * Vytvoří novou událost včetně dopočtu endTime, pokud chybí.
     */
    @Transactional
    public CalendarEvent createEvent(CalendarEvent event) {
        if (event.getCategory() == null) event.setCategory(ActivityType.OTHER);
        if (event.getTitle() == null || event.getTitle().isBlank())
            throw new RuntimeException("Title is required");

        // Dopočet konce události
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
     * Aktualizuje existující událost. Zachovává některé hodnoty z původní entity.
     */
    @Transactional
    public CalendarEvent updateEvent(UUID id, CalendarEvent updatedEvent) {
        return calendarEventRepository.findById(id)
                .map(existing -> {
                    if (updatedEvent.getUser() == null)
                        updatedEvent.setUser(existing.getUser());
                    if (updatedEvent.getCategory() == null)
                        updatedEvent.setCategory(existing.getCategory() != null ? existing.getCategory() : ActivityType.OTHER);

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
                    existing.setUser(updatedEvent.getUser());

                    // Logika výpočtu konce události
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
                .orElseThrow(() -> new RuntimeException("Událost nebyla nalezen

    @Transactional
    public void deleteEvent(UUID id) {
        notificationRepository.deleteAllByEventId(id);
        calendarEventRepository.deleteById(id);
    }
}
