package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name="training_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrainingSession {
    @Id
    @Column(name="event_id", columnDefinition = "uuid")
    private UUID eventId;

    @Column(name="sport_id")
    private Integer sportId;
}
