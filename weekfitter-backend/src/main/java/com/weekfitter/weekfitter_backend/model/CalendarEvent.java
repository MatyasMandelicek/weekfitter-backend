package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="calendar_events")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarEvent {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name="owner_id", columnDefinition = "uuid", nullable = false)
    private UUID ownerId;

    private String title;
    private String description;
    private String category;

    @Column(name="start_at")
    private OffsetDateTime startAt;

    @Column(name="end_at")
    private OffsetDateTime endAt;

    @Column(name="all_day")
    private Boolean allDay;

    private String location;
    private String recurrenceRule;
    private String color;

    @Column(columnDefinition = "text[]")
    private String[] tags;

    private String status;
}
