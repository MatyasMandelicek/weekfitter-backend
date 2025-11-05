package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.SportType;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import com.weekfitter.weekfitter_backend.service.NotificationService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class CalendarEventController {

    private final NotificationService notificationService;
    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;

    public CalendarEventController(
            CalendarEventService calendarEventService,
            UserRepository userRepository,
            CalendarEventRepository calendarEventRepository,
            NotificationService notificationService
    ) {
        this.calendarEventService = calendarEventService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<?> getEventsByUser(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByUser(userOpt.get());
        return ResponseEntity.ok(events);
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestParam String email, @RequestBody EventRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        User user = userOpt.get();

        try {
            CalendarEvent toSave = mapRequestToEntity(request);
            toSave.setUser(user);

            CalendarEvent saved = calendarEventService.createEvent(toSave);

            // === Notifikace ===
            notificationService.deleteByEvent(saved.getId());

            if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
                if (saved.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            LocalDateTime notifyAt = saved.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(saved, notifyAt);
                        }
                    }
                }
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Chyba při vytváření události: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable UUID id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody EventRequest request
    ) {
        try {
            User resolvedUser = null;
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
                }
                resolvedUser = userOpt.get();
            }

            CalendarEvent toUpdate = mapRequestToEntity(request);
            if (resolvedUser != null) {
                toUpdate.setUser(resolvedUser);
            }

            LocalDateTime oldStartTime = calendarEventService
                    .getEventById(id)
                    .map(CalendarEvent::getStartTime)
                    .orElse(null);

            // Aktualizuj událost
            CalendarEvent updated = calendarEventService.updateEvent(id, toUpdate);

            // === NOTIFIKACE ===
            if (request.getNotifications() != null) {
                // Frontend poslal nové notifikace -> smažeme a vytvoříme znovu
                notificationService.deleteByEvent(updated.getId());

                if (!request.getNotifications().isEmpty() && updated.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            LocalDateTime notifyAt = updated.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(updated, notifyAt);
                        }
                    }
                }
            } else {
                // Drag & drop – notifikace nejsou v requestu -> přepočítáme jejich časy
                if (oldStartTime != null && updated.getStartTime() != null
                        && !oldStartTime.equals(updated.getStartTime())) {
                    notificationService.rebaseExistingNotificationsToNewStart(
                            updated.getId(), oldStartTime, updated.getStartTime());
                }
            }

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Chyba při aktualizaci události: " + e.getMessage());
        }
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DTO ====================

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
        private List<Integer> notifications; // minuty před začátkem
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
}
