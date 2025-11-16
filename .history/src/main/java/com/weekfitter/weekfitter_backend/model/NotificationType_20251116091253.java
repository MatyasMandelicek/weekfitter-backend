package com.weekfitter.weekfitter_backend.model;

/**
 * Výčtový typ (enum) reprezentující jednotlivé typy notifikací,
 * které určují, kolik minut před začátkem události má být uživatel upozorněn.
 *
 * Každá hodnota obsahuje příslušný časový offset vyjádřený v minutách.
 */
public enum NotificationType {
    MINUTES_5(5),
    MINUTES_15(15),
    MINUTES_30(30),
    HOUR_1(60),
    HOURS_2(120),
    DAY_1(1440),
    DAYS_2(2880),
    WEEK_1(10080);

    /** Počet minut před začátkem události, kdy se má odeslat notifikace. */
    private final int minutesBefore;

    NotificationType(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    /** Vrátí počet minut přiřazený danému typu. */
    public int getMinutesBefore() {
        return minutesBefore;
    }

    /**
     * Vyhledá NotificationType podle jeho offsetu v minutách.
     * Používá se při vytváření notifikací z frontendové hodnoty (např. 30 → MINUTES_30).
     *
     * @param minutes počet minut před začátkem
     * @return odpovídající NotificationType nebo null, pokud neexistuje
     */
    public static NotificationType fromMinutes(int minutes) {
        for (NotificationType type : values()) {
            if (type.minutesBefore == minutes) return type;
        }
        return null;
    }
}
