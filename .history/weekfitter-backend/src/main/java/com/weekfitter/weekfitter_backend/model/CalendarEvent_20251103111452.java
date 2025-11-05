package com.weekfitter.weekfitter_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entita reprezentující událost v kalendáři uživatele.
 * Obsahuje základní informace o aktivitě a její časové vymezení.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    /** Jedinečný identifikátor události. */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** Název události (povinný). */
    @Column(nullable = false)
    private String title;

    /** Popis nebo poznámka k události. */
    private String description;

    /** Datum a čas začátku události. */
    private LocalDateTime startTime;
    
    /** Datum a čas konce události. */
    private LocalDateTime endTime;

    /** Typ aktivity (např. SPORT, WORK, REST). */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ActivityType category;

    /** Typ sportu, pokud se jedná o sportovní aktivitu. */
    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type")
    private SportType sportType;

    /** Příznak, zda událost trvá celý den. */
    @Column(name = "all_day")
    private boolean allDay;

    /** Doba trvání v minutách. */
    private Double duration;

    /** Plániovaná vzdálenost v kilometrech (u sportů). */
    private Double distance;

    private String sportDescription;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // ====== VÍCE NOTIFIKACÍ (nahrazuje původní notify/notifyBefore) ======
    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    // ====== Automatické výchozí hodnoty ======
    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (category == null) category = ActivityType.OTHER;

        // Automatické dopočítání konce podle délky nebo výchozí +1h
        if (startTime != null) {
            if (duration != null && duration > 0) {
                endTime = startTime.plusMinutes(duration.longValue());
            } else if (endTime == null || endTime.isBefore(startTime)) {
                endTime = startTime.plusHours(1);
            }
        }
    }

    // ====== Pomocné metody pro práci s notifikacemi ======
    public void addNotification(Notification n) {
        if (n == null) return;
        notifications.add(n);
        n.setEvent(this);
    }

    public void removeNotification(Notification n) {
        if (n == null) return;
        notifications.remove(n);
        n.setEvent(null);
    }
}
