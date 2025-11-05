package com.weekfitter.weekfitter_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entita reprezentující uživatele systému WeekFitter.
 * 
 * Uchovává osobní údaje, přihlašovací informace, volitelné fotografie
 * a data potřebná pro obnovu hesla.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {

    /** Jedinečný identifikátor uživatele (UUID). */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** Pohlaví uživatele – hodnota výčtového typu Gender. */
    @Enumerated(EnumType.STRING)
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

    @Column(name = "token_expiration")
    private LocalDateTime tokenExpiration;

}
