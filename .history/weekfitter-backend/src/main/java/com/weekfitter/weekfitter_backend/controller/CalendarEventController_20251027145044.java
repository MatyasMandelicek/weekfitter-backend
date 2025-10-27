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
        return ResponseEntity.ok(calendarEventService.createEvent(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable UUID id,
            @RequestParam(required = false) String email,
            @RequestBody CalendarEvent updatedEvent
    ) {
        if (email != null && !email.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Uživatel s e-mailem " + email + " nenalezen.");
            }
            updatedEvent.setUser(userOpt.get());
        }
        return ResponseEntity.ok(calendarEventService.updateEvent(id, updatedEvent));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        calendarEventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
