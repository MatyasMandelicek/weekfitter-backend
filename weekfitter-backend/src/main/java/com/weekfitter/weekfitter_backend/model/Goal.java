package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
import java.time.LocalDate;

@Data
@Entity
@Table(name="goals")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="owner_id", columnDefinition="uuid")
    private UUID ownerId;

    @Column(name="sport_id")
    private Integer sportId;

    private String type;
    private String period;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate;

    @Column(name="target_value")
    private Double targetValue;

    private String unit;
    private String status;
}
