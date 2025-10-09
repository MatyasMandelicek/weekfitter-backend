package com.weekfitter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name="integration_accounts")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationAccount {
    @Id
    @GeneratedValue(generator="UUID")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UuidGenerator")
    @Column(columnDefinition="uuid")
    private UUID id;

    @Column(name="user_id", columnDefinition="uuid")
    private UUID userId;

    private String provider;

    @Column(name="access_token")
    private String accessToken;

    @Column(name="refresh_token")
    private String refreshToken;

    private String scope;

    @Column(name="expires_at")
    private OffsetDateTime expiresAt;

    @Column(name="last_sync_at")
    private OffsetDateTime lastSyncAt;
}
