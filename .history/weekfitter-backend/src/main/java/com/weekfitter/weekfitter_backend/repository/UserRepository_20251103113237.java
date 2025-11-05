package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository rozhraní pro entitu User.
 * 
 * Poskytuje základní CRUD operace a vlastní metody
 * pro vyhledávání uživatelů podle e-mailu nebo reset tokenu.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /** Vyhledání uživatele podle e-mailové adresy. */
    Optional<User> findByEmail(String email);
    
    Optional<User> findByResetToken(String token);

}
