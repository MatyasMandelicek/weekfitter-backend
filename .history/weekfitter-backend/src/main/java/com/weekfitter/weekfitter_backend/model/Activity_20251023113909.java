package com.weekfitter.weekfitter_backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "activities")
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Activity {

@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private UUID id;


    @Column(nullable = false)
    private String name; // Např. "Running", "Cycling"

    private String description;
    private int durationMinutes;
    private double distanceKm;
    private int caloriesBurned;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
