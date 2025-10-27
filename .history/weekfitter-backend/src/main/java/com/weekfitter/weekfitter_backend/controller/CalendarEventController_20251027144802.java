package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;
    private final UserRepository userRepository;

    public CalendarEventController(CalendarEventService calendarEventService, UserRepository userRepository) {
        this.calendarEventService = calendarEventService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<?> getEventsByUser(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }
        return ResponseEntity.ok(calendarEventService.getEventsByUser(userOpt.get()));
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestParam String email, @RequestBody CalendarEvent event) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
        }

        event.setUser(userOpt.get());
        CalendarEvent saved = calendarEventService.createEvent(event);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody CalendarEvent updatedEvent
    ) {
        try {
            // doplnění uživatele, pokud přišel email
            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email);
                if (userOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
                }
                updatedEvent.setUser(userOpt.get());
            }
            CalendarEvent result = calendarEventService.updateEvent(id, updatedEvent);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
