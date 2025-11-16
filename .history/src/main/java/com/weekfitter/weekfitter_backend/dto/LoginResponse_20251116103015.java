package com.weekfitter.weekfitter_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO objekt vrácený po úspěšném přihlášení.
 *
 * Obsahuje:
 * - JWT token (Authorization),
 * - e-mail uživatele,
 * - křestní jméno (může se hodit pro FE).
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private String firstName;
}
