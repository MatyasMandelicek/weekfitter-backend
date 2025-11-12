package com.weekfitter.weekfitter_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;


/**
 * Hlavní třída aplikace WeekFitter Backend.
 * 
 * Spouští Spring Boot kontext a aktivuje plánování úloh (Scheduler),
 * které zajišťuje pravidelné odesílání notifikací.
 */
@SpringBootApplication
@EnableScheduling
public class WeekfitterBackendApplication {

    @PersistenceContext
    private EntityManager entityManager;

    public static void main(String[] args) {
        SpringApplication.run(WeekfitterBackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmUp() {
        try {
            entityManager.createQuery("SELECT 1").getSingleResult();
            System.out.println("Database connection pre-warmed.");
        } catch (Exception e) {
            System.err.println("⚠️ Database warm-up failed: " + e.getMessage());
        }
    }
}
