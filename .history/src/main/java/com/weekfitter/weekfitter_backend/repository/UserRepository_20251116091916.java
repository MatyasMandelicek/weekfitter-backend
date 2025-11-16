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

    /**
     * Vyhledá uživatele podle e-mailové adresy.
     *
     * Spring automaticky vytvoří odpovídající SQL dotaz:
     * SELECT * FROM users WHERE email = :email
     *
     * @param email e-mail uživatele
     * @return Optional obsahující nalezeného uživatele nebo prázdný, pokud neexistuje
     */
    Optional<User> findByEmail(String email);

    /**
     * Vyhledá uživatele podle reset tokenu používaného při obnově hesla.
     *
     * Token je náhodně generovaný jednorázový řetězec uložený v databázi,
     * který umožňuje identifikovat uživatele během procesu resetu hesla.
     *
     * @param token reset token
     * @return Optional uživatele, pokud token existuje a neexpiroval
     */
    Optional<User> findByResetToken(String token);

}
