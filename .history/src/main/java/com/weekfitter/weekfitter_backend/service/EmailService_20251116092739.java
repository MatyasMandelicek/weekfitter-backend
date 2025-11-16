package com.weekfitter.weekfitter_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Servisní třída pro odesílání e-mailů (reset hesla, notifikace).
 *
 * Namísto klasického SMTP protokolu (který je často blokován hostingy jako Render,
 * Railway nebo Vercel) využívá moderní Resend API přes HTTPS.
 *
 * Výhody:
 * - vysoká spolehlivost doručení,
 * - funguje na všech hostinzích,
 * - není potřeba nastavovat SMTP server nebo výjimky firewallu,
 * - jednoduchá integrace.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /** URL endpointu Resend API */
    private static final String RESEND_URL = "https://api.resend.com/emails";

    /** HttpClient pro odesílání HTTP požadavků. */
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /** API klíč služby Resend – načtený z environment proměnné. */
    private final String apiKey;

    /** Výchozí e-mailová adresa odesílatele (ve formátu „Název <email>“). */
    private final String fromEmail;

    /**
     * Konstruktor získávající potřebné hodnoty z application.properties
     * nebo environment proměnných.
     */
    public EmailService(
            @Value("${RESEND_API_KEY}") String apiKey,
            @Value("${RESEND_FROM_EMAIL}") String fromEmail
    ) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        log.info("EmailService inicializována s FROM: {}", this.fromEmail);
    }

    /**
     * Odešle uživateli e-mail s odkazem pro reset hesla.
     *
     * @param to    cílový e-mail
     * @param token reset token z databáze
     */
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = "https://weekfitter.vercel.app/reset-password/" + token;
        String subject = "Obnovení hesla - WeekFitter";
        String text = "Klikněte na následující odkaz pro obnovení hesla:\n\n" + resetLink;
        sendEmail(to, subject, text);
    }

    /**
     * Odesílá běžný informační nebo notifikační e-mail.
     *
     * @param to      cílový uživatel
     * @param subject předmět zprávy
     * @param body    text e-mailu
     */
    public void sendNotificationEmail(String to, String subject, String body) {
        sendEmail(to, subject, body);
    }

    /**
     * Hlavní metoda pro odeslání e-mailu přes Resend API.
     *
     * - vytváří JSON payload ručně,
     * - posílá POST request,
     * - loguje úspěch i chyby,
     * - nikdy nevyhazuje výjimky ven (scheduler by se nezastavil).
     */
    private void sendEmail(String to, String subject, String body) {
        try {

            // Vytvoření JSON payloadu
            String json = String.format("""
                {
                  "from": "%s",
                  "to": ["%s"],
                  "subject": "%s",
                  "text": "%s"
                }
                """,
                escapeJson(fromEmail),
                escapeJson(to),
                escapeJson(subject),
                escapeJson(body)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("E-mail úspěšně odeslán na {}", to);
            } else {
                log.error("Chyba při odesílání e-mailu ({}): {}", response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Nepodařilo se odeslat e-mail na {}: {}", to, e.getMessage());
        }
    }

    /**
     * Pomocná metoda pro escapování textu do JSON formátu.
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
