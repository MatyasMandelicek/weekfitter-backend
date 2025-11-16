package com.weekfitter.weekfitter_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Entita reprezentující jednu konkrétní notifikaci spojenou s událostí.
 *
 * Notifikace uchovává:
 * - datum a čas, kdy má být upozornění vyvoláno (notifyAt),
 * - informaci, zda už byla odeslána,
 * - typ notifikace (např. 5 minut před, 1 hodinu před, 1 den před),
 * - vazbu na událost, které se týká,
 * - vazbu na uživatele, jemuž patří.
 *
 * Notifikace jsou vytvářeny automaticky na základě nastavení události.
 * Jsou také přepočítávány při aktualizaci události (například po drag&drop).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {

    /** Primární klíč notifikace (UUID generované automaticky). */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Událost, ke které notifikace patří.
     *
     * optional = false → notifikace nemůže existovat bez události.
     * fetch = LAZY → událost se načte pouze při přístupu k ní.
     *
     * Poznámka: Na straně CalendarEvent je OneToMany s orphanRemoval,
     * což zaručuje automatické mazání notifikací při smazání události.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private CalendarEvent event;

    /**
     * Uživatel, kterému má být notifikace doručena.
     *
     * Ve většině případů jde o stejného uživatele, kterému patří událost,
     * ale vazba je uvedena explicitně pro případ rozšíření (např. sdílené události).
     *
     * fetch = LAZY → lepší výkon, uživatel se načítá jen při potřebe.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Datum a čas, kdy má být notifikace odeslána.
     *
     * Tento čas je vypočten na základě startTime události a offsetu
     * z enum typu NotificationType.
     */
    private LocalDateTime notifyAt;

    /** Příznak, zda již byla notifikace odeslána. */
    private boolean sent;

    /** Typ notifikace (5 minut, 1 den apod.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType notificationType;

}
