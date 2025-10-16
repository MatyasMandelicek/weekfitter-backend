package com.weekfitter.weekfitter_backend.respository;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, java.lang.Long> {
    List<CalendarEvent> findByUserId(Long userId);
}
