package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@Table(name="activity_types")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String category;
    private String unit;
}
