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

    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType category;

    @Column(name = "all_day")
    private boolean allDay;

    @Column(nullable = true)
    private Double duration;

    @Column(nullable = true)
    private Double distance;

    @Column(nullable = true, name = "sport_description")
    private String sportDescription;

    @ManyToOne(optional = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (category == null) {
            category = ActivityType.OTHER;
        }
        if (startTime != null && endTime == null) {
            endTime = startTime.plusMinutes(30);
        }
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            endTime = startTime.plusMinutes(30);
        }
    }
}
