package com.weekfitter.weekfitter_backend.model;

/**
 * Enum reprezentující různé typy notifikací podle času před začátkem události.
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

    private final int minutesBefore;

    NotificationType(int minutesBefore) {
        this.minutesBefore = minutesBefore;
    }

    public int getMinutesBefore() {
        return minutesBefore;
    }

    /**
     * Vrátí NotificationType podle počtu minut.
     */
    public static NotificationType fromMinutes(int minutes) {
        for (NotificationType type : values()) {
            if (type.minutesBefore == minutes) return type;
        }
        return null;
    }
}
