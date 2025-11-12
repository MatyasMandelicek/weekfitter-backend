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
import java.util.*;

/**
 * Controller třída zajišťující REST API pro práci s uživatelskými událostmi v kalendáři.
 *
 * Poskytuje CRUD operace (Create, Read, Update, Delete) a logiku pro propojení událostí
 * s notifikačním systémem.
 *
 * Notifikace se vytvářejí při přidání události, aktualizují při přesunutí
 * (např. pomocí drag & drop v kalendáři) a odstraňují při smazání události.
 */
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
     * Načte všechny události přihlášeného uživatele podle jeho e-mailu
     * včetně nastavených notifikací.
     */
    @GetMapping
    public ResponseEntity<?> getEventsByUser(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        List<CalendarEvent> events = calendarEventService.getEventsByUser(userOpt.get());

        // Každou událost doplníme o seznam notifikací (offsetů v minutách)
        List<EventResponse> response = new ArrayList<>();
        for (CalendarEvent e : events) {
            List<Integer> notificationOffsets = notificationService.getNotificationOffsetsForEvent(e);
            response.add(mapEntityToResponse(e, notificationOffsets));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Vytvoří novou událost pro daného uživatele a případně k ní založí notifikace.
     *
     * Logika:
     * - ověří, že uživatel existuje,
     * - uloží novou událost do databáze,
     * - smaže případné staré notifikace (pokud by existovaly),
     * - založí nové notifikace podle časových offsetů uvedených ve vstupním JSONu.
     *
     * @param email   e-mail uživatele, kterému událost patří
     * @param request datový objekt s parametry nové události
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

            // Notifikace
            notificationService.deleteByEvent(saved.getId());

            // Pokud má událost nastavené notifikace, vytvoříme je
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

    /**
     * Vrátí detail jedné konkrétní události podle jejího ID.
     *
     * @param id identifikátor události
     * @return detailní data o události
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
     * Metoda řeší dva různé scénáře:
     * 1. Pokud frontend posílá nové notifikace (např. úpravou události v detailu),
     *    původní notifikace se smažou a vytvoří nové.
     * 2. Pokud frontend notifikace neposílá (např. při přesunu události pomocí drag & drop),
     *    dojde pouze k přepočtu časů stávajících notifikací podle nového startu.
     *
     * @param id     ID události, která se má aktualizovat
     * @param email  volitelný e-mail uživatele (může být null)
     * @param request nové údaje události
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody EventRequest request
    ) {
        try {
            // Ověření uživatele (pokud je e-mail uveden)
            User resolvedUser = null;
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
                }
                resolvedUser = userOpt.get();
            }

            // Namapování requestu na entitu
            CalendarEvent toUpdate = mapRequestToEntity(request);
            if (resolvedUser != null) {
                toUpdate.setUser(resolvedUser);
            }

            // Získání původního času začátku pro případný přepočet notifikací
            LocalDateTime oldStartTime = calendarEventService
                    .getEventById(id)
                    .map(CalendarEvent::getStartTime)
                    .orElse(null);

            // Aktualizuj událost v databázi
            CalendarEvent updated = calendarEventService.updateEvent(id, toUpdate);

            // NOTIFIKACE
            if (request.getNotifications() != null) {
                // Frontend poslal nové notifikace - přemapujeme je
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
                // Drag & drop scénář - přepočítáme časy stávajících notifikací
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
     * Odstraní událost podle jejího ID.
     * Smazáním události se automaticky odstraní i všechny její notifikace.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== DTO ====================

    /**
     * Datový objekt (DTO) pro přenos dat mezi frontendem a backendem
     * při vytváření nebo aktualizaci události.
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
     * Pomocná metoda pro převod dat z DTO na entitu CalendarEvent.
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
     * Pomocná metoda pro převod entity CalendarEvent na DTO EventResponse.
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
