package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/**
 * Repository rozhraní pro entitu User.
 *
 * Rozšiřuje JpaRepository, takže poskytuje kompletní sadu CRUD operací
 * (uložení, smazání, aktualizace, hledání podle ID, výpis všech záznamů).
 *
 * Navíc obsahuje vlastní metody pro vyhledávání podle e-mailu
 * a podle reset tokenu pro obnovu hesla.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Vyhledání uživatele podle e-mailové adresy. */
    Optional<User> findByEmail(String email);

    /** Vyhledání uživatele podle tokenu pro reset hesla. */
    Optional<User> findByResetToken(String token);

}
