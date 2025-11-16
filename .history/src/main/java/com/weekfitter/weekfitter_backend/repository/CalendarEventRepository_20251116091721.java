package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.CalendarEvent;
import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Repository rozhraní pro práci s entitou CalendarEvent.
 *
 * Rozšiřuje JpaRepository, čímž získává kompletní sadu CRUD operací:
 * - uložit (save),
 * - načíst podle ID (findById),
 * - vypsat všechny (findAll),
 * - smazat (delete),
 * - a další utility metody.
 *
 * Dále obsahuje vlastní dotazovou metodu pro načtení událostí konkrétního uživatele.
 */
@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, UUID> {

    /**
     * Vrací všechny události patřící danému uživateli.
     *
     * Spring automaticky vytvoří SQL dotaz na základě názvu metody:
     * SELECT * FROM calendar_events WHERE user_id = :user
     *
     * @param user uživatel, jehož události se mají načíst
     * @return seznam událostí uživatele
     */    List<CalendarEvent> findByUser(User user);
}
