package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.NotificationType;
import com.weekfitter.weekfitter_backend.model.SportType;
import com.weekfitter.weekfitter_backend.model.User;

import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import com.weekfitter.weekfitter_backend.service.NotificationService;
import com.weekfitter.weekfitter_backend.service.UserService;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST controller pro správu uživatelských událostí v kalendáři.
 *
 * Poskytuje funkce pro vytváření, načítání, aktualizaci a mazání událostí (CRUD).
 * Identita uživatele je ověřena pomocí JWT tokenu.
 */

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class CalendarEventController {

    private final NotificationService notificationService;
    private final CalendarEventService calendarEventService;
    private final UserService userService;

    /** Konstruktor s explicitními závislostmi. */
    public CalendarEventController(
            CalendarEventService calendarEventService,
            NotificationService notificationService,
            UserService userService
    ) {
        this.calendarEventService = calendarEventService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Vrátí všechny události přihlášeného uživatele.
     */
    @GetMapping
    public ResponseEntity<?> getEventsByUser(java.security.Principal principal) {

        String email = principal.getName();
        User user = userService.getUserOrThrow(email);

        List<CalendarEvent> events = calendarEventService.getEventsByUser(user);

        List<EventResponse> response = new ArrayList<>();
        for (CalendarEvent e : events) {
            List<Integer> offsets = notificationService.getNotificationOffsetsForEvent(e);
            response.add(mapEntityToResponse(e, offsets));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Vytvoří novou událost přihlášeného uživatele.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(
            java.security.Principal principal,
            @RequestBody EventRequest request
    ) {
        String email = principal.getName();
        User user = userService.getUserOrThrow(email);

        try {
            CalendarEvent toSave = mapRequestToEntity(request);
            toSave.setUser(user);

            CalendarEvent saved = calendarEventService.createEvent(toSave);

            // Notifikace
            notificationService.deleteByEvent(saved.getId());

            if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
                for (Integer minutes : request.getNotifications()) {
                    if (minutes != null && minutes > 0 && saved.getStartTime() != null) {
                        NotificationType type = NotificationType.fromMinutes(minutes);
                        LocalDateTime notifyAt = saved.getStartTime().minusMinutes(minutes.longValue());
                        notificationService.createNotification(saved, notifyAt, type);
                    }
                }
            }

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Chyba při vytváření události: " + e.getMessage());
        }
    }

    /**
     * Vrací detail jedné události podle ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable UUID id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aktualizuje existující událost přihlášeného uživatele.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            java.security.Principal principal,
            @RequestBody EventRequest request
    ) {
        String email = principal.getName();
        User user = userService.getUserOrThrow(email);

        try {
            // Původní start time pro přepočet notifikací
            LocalDateTime oldStartTime = calendarEventService.getEventById(id)
                    .map(CalendarEvent::getStartTime)
                    .orElse(null);

            // Převod DTO → entita
            CalendarEvent toUpdate = mapRequestToEntity(request);
            toUpdate.setUser(user);

            CalendarEvent updated = calendarEventService.updateEvent(id, toUpdate);

            // Kontrola vlastnictví
            if (!updated.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Nemáte oprávnění upravovat tuto událost.");
            }

            // ===== NOTIFIKACE =====
            if (request.getNotifications() != null) {
                notificationService.deleteByEvent(updated.getId());

                if (!request.getNotifications().isEmpty() && updated.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            NotificationType type = NotificationType.fromMinutes(minutes);
                            LocalDateTime notifyAt = updated.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(updated, notifyAt, type);
                        }
                    }
                }
            } else {
                // drag&drop případ
                if (oldStartTime != null &&
                        updated.getStartTime() != null &&
                        !oldStartTime.equals(updated.getStartTime())) {
                    notificationService.rebaseExistingNotificationsToNewStart(
                            updated.getId(), oldStartTime, updated.getStartTime());
                }
            }

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Chyba při aktualizaci události: " + e.getMessage());
        }
    }

    /**
     * Smaže událost přihlášeného uživatele.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable UUID id,
            java.security.Principal principal
    ) {
        String email = principal.getName();
        User user = userService.getUserOrThrow(email);

        Optional<CalendarEvent> existing = calendarEventService.getEventById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        if (!existing.get().getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Nemáte oprávnění mazat tuto událost.");
        }

        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ============ DTOs ============

    @Data
    static class EventRequest {
        private UUID id;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ActivityType category;
        private SportType sportType;
        private Boolean allDay;
        private Double duration;
        private Double distance;
        private String sportDescription;
        private String filePath;
        private List<Integer> notifications;
    }

    @Data
    static class EventResponse {
        private UUID id;
        private String title;
        private String description;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private ActivityType category;
        private SportType sportType;
        private Boolean allDay;
        private Double duration;
        private Double distance;
        private String sportDescription;
        private String filePath;
        private List<Integer> notifications;
    }

    private static CalendarEvent mapRequestToEntity(EventRequest r) {
        CalendarEvent e = new CalendarEvent();
        e.setId(r.getId());
        e.setTitle(r.getTitle());
        e.setDescription(r.getDescription());
        e.setStartTime(r.getStartTime());
        e.setEndTime(r.getEndTime());
        e.setCategory(r.getCategory());
        e.setSportType(r.getSportType());
        e.setAllDay(Boolean.TRUE.equals(r.getAllDay()));
        e.setDuration(r.getDuration());
        e.setDistance(r.getDistance());
        e.setSportDescription(r.getSportDescription());
        e.setFilePath(r.getFilePath());
        return e;
    }

    private static EventResponse mapEntityToResponse(CalendarEvent e, List<Integer> notifications) {
        EventResponse r = new EventResponse();
        r.setId(e.getId());
        r.setTitle(e.getTitle());
        r.setDescription(e.getDescription());
        r.setStartTime(e.getStartTime());
        r.setEndTime(e.getEndTime());
        r.setCategory(e.getCategory());
        r.setSportType(e.getSportType());
        r.setAllDay(e.isAllDay());
        r.setDuration(e.getDuration());
        r.setDistance(e.getDistance());
        r.setSportDescription(e.getSportDescription());
        r.setFilePath(e.getFilePath());
        r.setNotifications(notifications);
        return r;
    }
}
