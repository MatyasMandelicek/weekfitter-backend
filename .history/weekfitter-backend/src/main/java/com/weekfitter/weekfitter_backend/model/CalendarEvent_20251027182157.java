package com.weekfitter.weekfitter_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Column(name = "category", nullable = false)
    private ActivityType category; // SPORT, WORK, SCHOOL, REST, OTHER

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type")
    private SportType sportType;   // typ sportu (běh, kolo, plavání, jiné)

    @Column(name = "all_day")
    private boolean allDay;

    private Double duration;
    private Double distance;

    private String sportDescription;

    @Column(nullable = false)
    private Boolean notify;

    @Column(name = "notify_before")
    private Integer notifyBefore;


    @Column(name = "file_path")
    private String filePath; // uložený GPX/JSON soubor

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (category == null) category = ActivityType.OTHER;
        if (startTime != null && endTime == null) endTime = startTime.plusMinutes(30);
        if (startTime != null && endTime != null && endTime.isBefore(startTime))
            endTime = startTime.plusMinutes(30);
    }

    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

}
