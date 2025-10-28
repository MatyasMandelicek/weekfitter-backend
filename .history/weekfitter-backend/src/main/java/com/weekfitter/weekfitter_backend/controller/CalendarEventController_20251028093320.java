package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.SportType;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
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

    /**
     * Vrátí události pouze přihlášeného uživatele podle e-mailu
     */
    @GetMapping
    public ResponseEntity<?> getEventsByUser(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByUser(userOpt.get());
        return ResponseEntity.ok(events);
    }

    /**
     * Vytvoří novou událost a automaticky ji přiřadí k uživateli.
     * Podporuje více notifikací (pole minut). Pokud nejsou poslány,
     * funguje zpětně kompatibilně přes notify/notifyBefore.
     */
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

            // ---- Notifikace: nejdřív zrušit stávající (pro jistotu) ----
            notificationService.deleteByEvent(saved.getId());

            // Preferuj pole notifications (více notifikací)
            if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
                if (saved.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            LocalDateTime notifyAt = saved.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(saved, notifyAt);
                        }
                    }
                }
            } else {
                // Zpětná kompatibilita: single notify + notifyBefore
                if (Boolean.TRUE.equals(saved.getNotify()) && saved.getStartTime() != null) {
                    int notifyBefore = (saved.getNotifyBefore() != null) ? saved.getNotifyBefore() : 60;
                    LocalDateTime notifyAt = saved.getStartTime().minusMinutes(notifyBefore);
                    notificationService.createNotification(saved, notifyAt);
                }
            }

            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Chyba při vytváření události: " + e.getMessage());
        }
    }

    /**
     * Vrátí detail události podle ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable UUID id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aktualizace existující události.
     * Podporuje více notifikací (pole minut). Pokud nejsou poslány,
     * funguje zpětně kompatibilně přes notify/notifyBefore.
     */
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

            CalendarEvent updated = calendarEventService.updateEvent(id, toUpdate);

            // ---- Notifikace: smazat staré, vytvořit nové ----
            notificationService.deleteByEvent(updated.getId());

            if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
                if (updated.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            LocalDateTime notifyAt = updated.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(updated, notifyAt);
                        }
                    }
                }
            } else {
                // Zpětná kompatibilita: single notify + notifyBefore
                if (Boolean.TRUE.equals(updated.getNotify()) && updated.getStartTime() != null) {
                    int notifyBefore = (updated.getNotifyBefore() != null) ? updated.getNotifyBefore() : 60;
                    LocalDateTime newNotifyAt = updated.getStartTime().minusMinutes(notifyBefore);
                    notificationService.createNotification(updated, newNotifyAt);
                }
            }

            return ResponseEntity.ok(updated);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Chyba při aktualizaci události: " + e.getMessage());
        }
    }

    /**
     * Smazání události
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Pomocné DTO a mapper ====================

    /**
     * Interní request DTO – rozšiřuje CalendarEvent o pole "notifications" (minuty před začátkem).
     * Necháváme ho v tomto souboru kvůli jednoduché integraci.
     */
    @Data
    static class EventRequest {
        private UUID id;

        private String title;
        private String description;

        private LocalDateTime startTime;
        private LocalDateTime endTime;

        private ActivityType category; // SPORT, WORK, SCHOOL, REST, OTHER
        private SportType sportType;

        private Boolean allDay;

        private Double duration;
        private Double distance;

        private String sportDescription;

        private String filePath;

        /**
         * Nové pole: více notifikací v minutách před začátkem.
         * Např. [5, 60, 1440] = 5 min, 1 hod, 1 den.
         */
        private List<Integer> notifications;

        // Zpětná kompatibilita – stále přijímáme, ale preferujeme "notifications"
        private Boolean notify;
        private Integer notifyBefore;
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

        // Zpětná kompatibilita – tyto dvě pole mohou zůstat v entitě,
        // ale do budoucna je můžeš odstranit. Pokud posíláš notifications[],
        // controller je stejně ignoruje.
        e.setNotify(r.getNotify());
        e.setNotifyBefore(r.getNotifyBefore());

        return e;
    }
}
