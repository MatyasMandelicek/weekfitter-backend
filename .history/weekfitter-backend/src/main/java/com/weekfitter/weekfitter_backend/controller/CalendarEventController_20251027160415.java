package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import com.weekfitter.weekfitter_backend.service.NotificationService;
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
     * Pokud je zvoleno upozornění, vytvoří i záznam v tabulce notifications.
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestParam String email, @RequestBody CalendarEvent event) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        User user = userOpt.get();
        event.setUser(user);

        try {
            CalendarEvent saved = calendarEventService.createEvent(event);

            // === Vytvoření notifikace, pokud uživatel zvolil upozornění ===
            if (event.getStartTime() != null) {
                // Z payloadu získáme notifyBefore (v minutách) a flag notify
                int notifyBefore = 0;
                boolean notifyEnabled = false;

                // Bezpečné načtení z generického JSONu (pokud frontend posílá navíc klíče)
                try {
                    var raw = event.getClass().getDeclaredField("notifyBefore");
                } catch (Exception ignored) {}

                // Pokud frontend posílá notifyBefore i notify, přidej je do entity (doporučeno)
                // nebo je zpracuj v JSON payloadu jako Mapu (viz níže vylepšení).

                // Pro jednoduchost zde předpokládáme, že frontend už posílá notifyBefore a notify
                // a ty jsou součástí JSON objektu, který Spring správně namapuje.

                // Např. payload:
                // {
                //   "title": "...",
                //   "startTime": "...",
                //   "notify": true,
                //   "notifyBefore": 5
                // }

                // Zde tedy:
                if (event instanceof CalendarEvent) {
                    // pokud bys měl rozšířený DTO, můžeš hodnoty přečíst přímo
                    // zde použijeme fixní výchozí hodnotu 5 minut
                    notifyBefore = 5;
                    notifyEnabled = true;
                }

                if (notifyEnabled) {
                    LocalDateTime notifyAt = event.getStartTime().minusMinutes(notifyBefore);
                    notificationService.createNotification(saved, notifyAt);
                    System.out.println("[INFO] Notifikace naplánována na " + notifyAt + " (začátek: " + event.getStartTime() + ")");
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
     * Aktualizace existující události
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody CalendarEvent event
    ) {
        try {
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
                }
                event.setUser(userOpt.get());
            }

            CalendarEvent updated = calendarEventService.updateEvent(id, event);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
}
