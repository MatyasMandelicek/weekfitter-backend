package com.weekfitter.weekfitter_backend.model;

/**
 * Výčtový typ (enum) představující pohlaví uživatele.
 *
 * Používá se jako součást uživatelského profilu. Hodnoty jsou ukládány
 * do databáze ve formátu STRING a umožňují jednoduché filtrování a zobrazení.
 *
 * Enum je volitelný — uživatel nemusí pohlaví uvést.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER
}
