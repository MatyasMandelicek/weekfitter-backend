package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.service.CalendarEventService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*")
public class CalendarEventController {

    private final CalendarEventService service;

    public CalendarEventController(CalendarEventService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public List<CalendarEvent> getEvents(@PathVariable Long userId) {
        return service.getEventsByUser(userId);
    }

    @PostMapping
    public CalendarEvent addEvent(@RequestBody CalendarEvent event) {
        return service.addEvent(event);
    }

    @DeleteMapping("/{id}")
    public void deleteEvent(@PathVariable Long id) {
        service.deleteEvent(id);
    }
}
