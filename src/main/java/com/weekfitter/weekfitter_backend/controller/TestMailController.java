package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestMailController {

    private final EmailService emailService;

    @GetMapping("/mail")
    public ResponseEntity<String> testMail() {
        emailService.sendNotificationEmail("matyas.mandelic@gmail.com",
                "Test z WeekFitter (Resend)",
                "Toto je testovací e-mail odeslaný přes Resend API.");
        return ResponseEntity.ok("Testovací e-mail odeslán – zkontroluj logy a schránku.");
    }
}
