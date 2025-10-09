package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@Table(name="user_settings")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {
    @Id
    @Column(name="user_id", columnDefinition="uuid")
    private UUID userId;

    @Column(name="preferred_unit_system", length=10)
    private String preferredUnitSystem;

    @Column(name="language", length=10)
    private String language;

    @Column(name="theme", length=10)
    private String theme;

    @Column(name="notifications_enabled")
    private Boolean notificationsEnabled;
}
