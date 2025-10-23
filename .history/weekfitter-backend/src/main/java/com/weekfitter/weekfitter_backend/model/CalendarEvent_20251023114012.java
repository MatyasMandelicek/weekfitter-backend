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

@ManyToOne
@JoinColumn(name = "user_id", referencedColumnName = "id")
private User user;
}
