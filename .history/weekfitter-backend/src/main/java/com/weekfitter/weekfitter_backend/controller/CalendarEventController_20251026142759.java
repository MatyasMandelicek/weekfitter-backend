package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;

    public CalendarEventController(CalendarEventService calendarEventService, UserRepository userRepository) {
        this.calendarEventService = calendarEventService;
        this.userRepository = userRepository;
    }

    /**
     * ✅ Načte události pouze pro daného uživatele (podle e-mailu)
     * např. GET /api/events?email=jan@novak.cz
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
     * ✅ Načte událost podle ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalendarEvent> getEventById(@PathVariable UUID id) {
        return calendarEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ✅ Vytvoří novou událost přiřazenou uživateli podle e-mailu
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestParam String email, @RequestBody CalendarEvent event) {
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
            }

            event.setUser(userOpt.get());
            CalendarEvent saved = calendarEventService.createEvent(event);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Aktualizace existující události
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable UUID id, @RequestBody CalendarEvent event) {
        try {
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
