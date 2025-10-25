package com.weekfitter.weekfitter_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "calendar_events")
public class CalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType category;

    private boolean allDay;

    // Sportovní atributy (nepovinné)
    @Column(nullable = true)
    private Double duration;

    @Column(nullable = true)
    private Double distance;

    @Column(nullable = true)
    private String sportDescription;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
