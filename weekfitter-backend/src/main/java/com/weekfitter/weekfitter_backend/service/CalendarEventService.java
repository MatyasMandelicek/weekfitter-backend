package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.respository.CalendarEventRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public CalendarEventService(CalendarEventRepository calendarEventRepository) {
        this.calendarEventRepository = calendarEventRepository;
    }

    public List<CalendarEvent> getAllEvents() {
        return calendarEventRepository.findAll();
    }

    public CalendarEvent saveEvent(CalendarEvent event) {
        return calendarEventRepository.save(event);
    }
}
