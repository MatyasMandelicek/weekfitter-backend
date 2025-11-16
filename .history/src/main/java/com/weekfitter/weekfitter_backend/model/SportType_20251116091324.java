package com.weekfitter.weekfitter_backend.model;

/**
 * Výčtový typ (enum) určující konkrétní typ sportovní aktivity.
 *
 * Slouží k přesnější klasifikaci událostí označených jako SPORT.
 * Umožňuje zobrazovat specifické ikony, statistiky nebo výpočty 
 * (např. tempo u běhu, watty u cyklistiky apod.).
 */
public enum SportType {
    RUNNING,
    CYCLING,
    SWIMMING,
    OTHER
}
