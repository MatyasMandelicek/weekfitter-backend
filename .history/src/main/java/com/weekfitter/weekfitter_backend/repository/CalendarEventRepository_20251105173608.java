package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository rozhraní pro entitu CalendarEvent.
 * 
 * Umožňuje načítání událostí podle přiřazeného uživatele.
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    /** Vrací všechny události vytvořené konkrétním uživatelem. */
    List<CalendarEvent> findByUser(User user);
}
