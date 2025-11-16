package com.weekfitter.weekfitter_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Hlavní spouštěcí třída backendové aplikace WeekFitter.
 *
 * Tato třída:
 * - inicializuje Spring Boot framework,
 * - spustí embedded servlet kontejner (Tomcat),
 * - nakonfiguruje komponenty a auto-konfiguraci,
 * - po startu provede "warm-up" databázového připojení, aby byl
 *   první API request rychlejší.
 *
 * Warm-up je užitečný hlavně u cloudových hostingů, kde Hibernate
 * inicializuje entity až při prvním skutečném dotazu.
 */
@SpringBootApplication
public class WeekfitterBackendApplication {

    /**
     * Poskytuje přístup do persistence contextu (JPA).
     * Je injektován pomocí anotace @PersistenceContext.
     *
     * Slouží zde výhradně pro inicializační dotaz po startu aplikace.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Hlavní metoda spouštějící Spring Boot aplikaci.
     */
    public static void main(String[] args) {
        SpringApplication.run(WeekfitterBackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            entityManager.createQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection pre-warmed.");
        } catch (Exception e) {
            System.err.println("Database warm-up failed: " + e.getMessage());
        }
    }
}
