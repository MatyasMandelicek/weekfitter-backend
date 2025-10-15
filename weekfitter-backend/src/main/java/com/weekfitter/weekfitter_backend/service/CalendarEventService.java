package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CalendarEventService {

    private final CalendarEventService repository;

    public CalendarEventService(CalendarEventService repository) {
        this.repository = repository;
    }

    public List<CalendarEvent> getEventsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    private List<CalendarEvent> findByUserId(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByUserId'");
    }

    public CalendarEvent addEvent(CalendarEvent event) {
        return repository.save(event);
    }

    private CalendarEvent save(CalendarEvent event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

    public void deleteEvent(Long id) {
        repository.deleteById(id);
    }

    private void deleteById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }
}
