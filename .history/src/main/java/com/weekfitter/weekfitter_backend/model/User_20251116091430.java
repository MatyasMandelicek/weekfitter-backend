package com.weekfitter.weekfitter_backend.model;

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
     */    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** Křestní jméno uživatele. */
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /** Příjmení uživatele. */
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Emailová adresa uživatele, zároveň unikátní přihlašovací identifikátor */
    @Column(nullable = false, unique = true)
    private String email;

    /** Heslo uživatele, uloženo jako hash pro bezpečnost. */
    @Column(nullable = false)
    private String password;

    /** Datum narození uživatele (volitelné). */
    @Column(name = "birth_date")
    private LocalDate birthDate;

    /** Cesta k profilové fotografii uložené na serveru. */
    private String photo;

    /** Token pro reset hesla – generován při žádosti o obnovu. */
    @Column(name = "reset_token")
    private String resetToken;

    /** Datum a čas expirace reset tokenu. */
    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

}
