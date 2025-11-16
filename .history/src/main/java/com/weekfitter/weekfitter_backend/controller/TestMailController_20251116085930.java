package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Testovací controller pro ověření funkčnosti odesílání e-mailů.
 *
 * Tento controller slouží pouze pro interní účely během vývoje a umožňuje
 * rychle otestovat, zda je:
 * - nakonfigurované Resend API,
 * - funkční EmailService,
 * - správně fungující odesílání e-mailů z backendu.
 *
 * Po nasazení do produkce by měl být tento endpoint běžně vypnutý
 * nebo chráněný, protože jinak umožňuje komukoliv odesílat libovolné e-maily.
 */
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestMailController {

    /** Služba starající se o odesílání e-mailů přes Resend API. */
    private final EmailService emailService;
¨

    @GetMapping("/mail")
    public ResponseEntity<String> testMail() {
        emailService.sendNotificationEmail("matyas.mandelic@gmail.com",
                "Test z WeekFitter (Resend)",
                "Toto je testovací e-mail odeslaný přes Resend API.");
        return ResponseEntity.ok("Testovací e-mail odeslán – zkontroluj logy a schránku.");
    }
}
