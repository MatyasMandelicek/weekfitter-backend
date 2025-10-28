package com.weekfitter.weekfitter_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private SportType sportType;

    @Column(name = "all_day")
    private boolean allDay;

    private Double duration;
    private Double distance;

    private String sportDescription;

    // ——— Notifikace k události ———
    // Držme se Boolean, ale zajistíme defaulty v @PrePersist/@PreUpdate
    @Column(nullable = false)
    private Boolean notify;

    @Column(name = "notify_before")
    private Integer notifyBefore;

    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Zabraň cyklické serializaci a nastav výchozí prázdný seznam i při použití Lombok builderu
    @JsonIgnore
    @OneToMany(mappedBy = "event", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (category == null) category = ActivityType.OTHER;

        // Bezpečné dopočítání endTime
        if (startTime != null) {
            if (duration != null) {
                endTime = startTime.plusMinutes(duration.longValue());
            } else if (endTime == null) {
                endTime = startTime.plusMinutes(30); // fallback
            }
            if (endTime.isBefore(startTime)) {
                endTime = startTime.plusMinutes(30);
            }
        }

        // Defaulty pro notifikace, aby nikdy nepadaly na NULL
        if (notify == null) notify = false;
        if (Boolean.TRUE.equals(notify) && notifyBefore == null) {
            notifyBefore = 60; // např. 60 min default
        }
    }

    // Volitelné helpery – IDE pak obvykle přestane hlásit „unused“
    public void addNotification(Notification n) {
        notifications.add(n);
        n.setEvent(this);
    }

    public void removeNotification(Notification n) {
        notifications.remove(n);
        n.setEvent(null);
    }
}
