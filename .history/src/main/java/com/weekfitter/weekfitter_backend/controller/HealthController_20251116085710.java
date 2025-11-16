package com.weekfitter.weekfitter_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Jednoduchý health-check controller, který slouží k ověření,
 * že backend běží a je schopen odpovídat na HTTP požadavky.
 *
 * Tento endpoint je využitelný například:
 * - jako kontrolní bod pro monitoring (např. UptimeRobot, Vercel, Docker, Kubernetes),
 * - pro CI/CD platformy ověřující dostupnost po nasazení,
 * - pro interní diagnostiku při ladění.
 *
 * Vrací čistý text "OK", což postačuje pro základní kontrolu.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
