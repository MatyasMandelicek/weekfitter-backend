package com.weekfitter.weekfitter_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entita reprezentující uživatele systému WeekFitter.
 *
 * Uchovává:
 * - identifikační údaje (jméno, příjmení, e-mail),
 * - přihlašovací údaje (heslo – hash),
 * - volitelné osobní údaje (pohlaví, datum narození, profilové foto),
 * - informace potřebné pro proces obnovy hesla (reset token + expirace).
 *
 * Objekt je perzistován v tabulce "users" a slouží jako hlavní model
 * pro autentizaci a práci s profilem uživatele.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    /** 
     * Primární klíč uživatele.
     * UUID zajišťuje globální unikátnost bez nutnosti sekvencí.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** 
     * Pohlaví uživatele. 
     * Enum Gender umožňuje jednoduché rozšíření nebo filtrování.
     */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** Křestní jméno uživatele (povinné). */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Příjmení uživatele (povinné). */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * E-mailová adresa uživatele.
     * - povinná,
     * - unikátní,
     * - používána jako přihlašovací jméno (username).
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Heslo uložené v podobě hashované hodnoty (BCrypt).
     * Přijímá se z frontendu, ale nikdy se neposílá zpět.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    /** Volitelné datum narození. */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Cesta k profilové fotografii. */
    private String photo;

    /**
     * Token pro obnovu hesla.
     * - generuje se pouze při žádosti o reset,
     * - je jednorázový a krátkodobý.
     */
    @JsonIgnore
    @Column(name = "reset_token")
    private String resetToken;

    /** Datum a čas expirace reset tokenu. */
    @JsonIgnore
    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

}
