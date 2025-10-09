package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
import java.time.LocalDate;

@Data
@Entity
@Table(name="stats_snapshots")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsSnapshot {
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="owner_id", columnDefinition="uuid")
    private UUID ownerId;

    @Column(name="sport_id")
    private Integer sportId;

    private String grain;

    @Column(name="period_start")
    private LocalDate periodStart;

    @Column(columnDefinition = "jsonb")
    private String totals;
}
