package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name="activities")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity {
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="owner_id", columnDefinition="uuid")
    private UUID ownerId;

    private String source;

    @Column(name="source_external_id")
    private String sourceExternalId;

    @Column(name="sport_id")
    private Integer sportId;

    @Column(name="start_at")
    private OffsetDateTime startAt;

    @Column(name="elapsed_sec")
    private Integer elapsedSec;

    @Column(name="moving_sec")
    private Integer movingSec;

    @Column(columnDefinition = "jsonb")
    private String metricsTotal;

    @Column(name="perceived_exertion")
    private Integer perceivedExertion;

    private String notes;

    @Column(name="matched_training_session_id", columnDefinition="uuid")
    private UUID matchedTrainingSessionId;

    @Column(columnDefinition = "jsonb")
    private String gpsData;
}
