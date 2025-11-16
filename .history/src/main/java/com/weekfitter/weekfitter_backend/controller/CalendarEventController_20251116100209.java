package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.ActivityType;
import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.NotificationType;
import com.weekfitter.weekfitter_backend.model.SportType;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.repository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.repository.UserRepository;
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
 * Dále také koordinuje logiku vytváření, aktualizace a mazání notifikací,
 * které jsou vázány ke konkrétní události.
 *
 * Controller pracuje s CalendarEventService a NotificationService, které řeší
 * samotnou byznys logiku a komunikaci s databází.
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class CalendarEventController {

    private final NotificationService notificationService;
    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Konstruktor inicializující závislosti controlleru.
     *
     * @param calendarEventService služba pro práci s událostmi
     * @param userRepository repozitář uživatelů
     * @param notificationService služba starající se o notifikace
     */
    public CalendarEventController(
            CalendarEventService calendarEventService,
            UserRepository userRepository,
            CalendarEventRepository calendarEventRepository,
            NotificationService notificationService
            Use
    ) {
        this.calendarEventService = calendarEventService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    /**
     * Načte všechny události konkrétního uživatele podle jeho e-mailu.
     * Ke každé události jsou doplněny její notifikace.
     *
     * @param email e-mail přihlášeného uživatele
     * @return seznam jeho událostí + notifikací
     */
    @GetMapping
    public ResponseEntity<?> getEventsByUser(@RequestParam String email) {
        User user = userService.getUserOrThrow(email);

        List<CalendarEvent> events = calendarEventService.getEventsByUser(user);

        // Doplnění notifikací ke každé události
        List<EventResponse> response = new ArrayList<>();
        for (CalendarEvent e : events) {
            List<Integer> notificationOffsets = notificationService.getNotificationOffsetsForEvent(e);
            response.add(mapEntityToResponse(e, notificationOffsets));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Vytvoří novou událost pro uživatele + založí k ní notifikace.
     *
     * @param email e-mail uživatele
     * @param request datová struktura popisující událost
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

            // Notifikace – nejprve smazat potenciální staré (ochrana proti duplicitám)
            notificationService.deleteByEvent(saved.getId());

            if (request.getNotifications() != null && !request.getNotifications().isEmpty()) {
                if (saved.getStartTime() != null) {
                    for (Integer minutes : request.getNotifications()) {
                        if (minutes != null && minutes > 0) {
                            NotificationType type = NotificationType.fromMinutes(minutes);
                            LocalDateTime notifyAt = saved.getStartTime().minusMinutes(minutes.longValue());
                            notificationService.createNotification(saved, notifyAt, type);
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

    /**
     * Vrátí detail jedné události.
     *
     * @param id id události
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable UUID id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Aktualizuje existující událost.
     *
     * Obsahuje komplexní logiku dvou scénářů:
     * 1) Frontend poslal nové notifikace → staré se smažou, vytvoří nové.
     * 2) Frontend neposlal nové notifikace → jedná se o drag&drop → pouze přepočet stávajících.
     *
     * @param id ID události
     * @param email nepovinný e-mail uživatele
     * @param request nové hodnoty události
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody EventRequest request
    ) {
        try {
            // Pokud je uveden e-mail, najdeme uživatele
            User resolvedUser = null;
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
                }
                resolvedUser = userOpt.get();
            }

            // Převod DTO → entita
            CalendarEvent toUpdate = mapRequestToEntity(request);
            if (resolvedUser != null) {
                toUpdate.setUser(resolvedUser);
            }

            // Původní start time (pro pozdější přepočet notifikací)
            LocalDateTime oldStartTime = calendarEventService
                    .getEventById(id)
                    .map(CalendarEvent::getStartTime)
                    .orElse(null);

            // Aktualizuj událost v databázi
            CalendarEvent updated = calendarEventService.updateEvent(id, toUpdate);

            // ==================
            // === NOTIFIKACE ===
            // ==================

            if (request.getNotifications() != null) {
                // 1) byly poslány nové hodnoty → smažeme staré → vytvoříme nové
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
                // 2) nebyly poslány → jedná se o přesun (drag&drop)
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

    /**
     * Smaže událost. Smazáním události se automaticky odstraní i všechny notifikace.
     *
     * @param id ID události
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // =======================================================================
    // =========================== DTO TŘÍDY =================================
    // =======================================================================

    /**
     * DTO pro vytváření nebo aktualizaci události.
     */
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

    /**
     * DTO pro vrácení události včetně nastavených notifikací.
     */
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
        private List<Integer> notifications; // minuty před začátkem
    }

    /**
     * Mapuje EventRequest → CalendarEvent.
     */
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
    
    /**
     * Mapuje CalendarEvent → EventResponse.
     */
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
