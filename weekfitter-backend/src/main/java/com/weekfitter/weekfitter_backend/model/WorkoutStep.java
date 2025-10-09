package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name="workout_steps")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutStep {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name="training_session_id", columnDefinition = "uuid")
    private UUID trainingSessionId;

    @Column(name="step_order")
    private Integer stepOrder;

    private String type;

    @Column(columnDefinition = "jsonb")
    private String target;

    @Column(name="measure_by")
    private String measureBy;
}
