package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.weekfitter.weekfitter_backend.service.NotificationService;
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
     * Vytvoří novou událost a automaticky ji přiřadí k uživateli
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestParam String email, @RequestBody CalendarEvent event) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        User user = userOpt.get();
        event.setUser(user); // napojení události na uživatele

        try {
            CalendarEvent saved = calendarEventService.createEvent(event);

            // === Vytvoření notifikace podle volby uživatele ===
            if (event.getStartTime() != null) {
                // Zatím pevně hodinu předem — frontend ti pošle customNotifyMinutes později
                LocalDateTime notifyAt = event.getStartTime().minusMinutes(60);
                notificationService.createNotification(saved, notifyAt);
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
     * - pokud je dodán email, událost se k tomuto uživateli přiváže
     * - pokud email není, zachová se původní uživatel
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
