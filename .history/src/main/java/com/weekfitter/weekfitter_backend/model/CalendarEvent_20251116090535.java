package com.weekfitter.weekfitter_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entita reprezentující událost v uživatelském kalendáři.
 *
 * Obsahuje základní popis události, časové vymezení (start/konec), 
 * informace o kategorii aktivity (Sport, Práce, Odpočinek, …) 
 * a případné sportovní parametry (vzdálenost, trvání apod.).
 *
 * Událost je v databázi navázaná na konkrétního uživatele (ManyToOne)
 * a může mít přiřazené notifikace (OneToMany), které se automaticky smažou
 * při odstranění události.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    /** Primární klíč – jednoznačný identifikátor události. */
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

    /** Plánovaná vzdálenost v kilometrech (u sportů). */
    private Double distance;

    /** Doprovodný text popisující sportovní aktivitu. */
    private String sportDescription;

    /** Cesta k souboru (např. záznam trasy, obrázek, příloha). */
    @Column(name = "file_path")
    private String filePath;

    /** Vazba na uživatele, který událost vytvořil. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Seznam notifikací spojených s událostí. */
    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Automatické nastavení výchozích hodnot před uložením nebo aktualizací.
     * Např. výpočet konce události, pokud není explicitně zadán.
     */
    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (category == null) category = ActivityType.OTHER;

        if (startTime != null) {
            if (duration != null && duration > 0) {
                endTime = startTime.plusMinutes(duration.longValue());
            } else if (endTime == null || endTime.isBefore(startTime)) {
                endTime = startTime.plusHours(1);
            }
        }
    }

    // Pomocné metody pro práci s notifikacemi
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
