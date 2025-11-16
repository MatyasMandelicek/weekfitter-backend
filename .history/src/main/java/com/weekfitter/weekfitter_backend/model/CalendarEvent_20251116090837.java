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

    /** Nepovinný textový popis nebo poznámka k události. */
    private String description;

    /** Datum a čas začátku události. */
    private LocalDateTime startTime;
    
    /** Datum a čas konce události. */
    private LocalDateTime endTime;

    /** Kategorie události – viz výčtový typ ActivityType. */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ActivityType category;

    /** Konkrétní sport (pokud se jedná o sportovní aktivitu). */
    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type")
    private SportType sportType;

    /** Příznak indikující, zda událost trvá celý den. */
    @Column(name = "all_day")
    private boolean allDay;

    /**
     * Doba trvání události v minutách.
     * 
     * Poznámka:
     * - pokud je trvání vyplněno, endTime se přepočítá automaticky.
     * - jinak se při ukládání nastaví výchozí trvání 1 hodina.
     */
    private Double duration;

    /** Očekávaná vzdálenost v kilometrech (sport). */
    private Double distance;

    /** Speciální popis sportovní aktivity (např. typ tréninku). */
    private String sportDescription;

    /** Cesta k připojenému souboru (např. GPX trasa, json dokument). */
    @Column(name = "file_path")
    private String filePath;

    /**
     * Vazba na uživatele, kterému tato událost patří.
     *
     * FetchType.LAZY — uživatel se nenačítá automaticky, což je správné pro výkon.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Seznam notifikací asociovaných k události.
     *
     * - mappedBy = "event": říká, že vlastníkem vazby je entita Notification
     * - cascade = REMOVE: při smazání události se smažou všechny notifikace
     * - orphanRemoval = true: zaručí smazání upozornění, která jsou odstraněna z kolekce
     *
     * @JsonIgnore — notifikace se nevrací v API, posílají se pouze jejich offsety.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Lifecycle hook volaný před uložením nebo aktualizací entity.
     *
     * Slouží k doplnění výchozích hodnot:
     * - Pokud není zadána kategorie → nastaví se OTHER.
     * - Pokud je zadán startTime a trvání → spočítá endTime.
     * - Pokud není endTime uveden nebo je chybně dříve než start → nastaví se start + 1 hodina.
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

    // ==========================
    // Pomocné metody manipulace
    // ==========================

    /**
     * Přidá notifikaci k události.
     * Zachová obousměrnou vazbu mezi CalendarEvent a Notification.
     */
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
