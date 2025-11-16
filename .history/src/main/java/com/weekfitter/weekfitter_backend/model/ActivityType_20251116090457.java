package com.weekfitter.weekfitter_backend.model;

/**
 * Výčtový typ (enum) představující jednotlivé kategorie aktivit,
 * které může uživatel v aplikaci vytvořit jako událost v kalendáři.
 *
 * Typ aktivity slouží ke třídění událostí a k jejich vizuálnímu rozlišení
 * ve frontendové části aplikace (barvou nebo ikonou).
 */public enum ActivityType {
    SPORT,
    WORK,
    SCHOOL,
    REST,
    OTHER
}
