package org.example.userservice.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    private String value;

    @ManyToOne
    private User user;

    /*
    private String token;
    private String tokenHash;
    private String deviceInfo;
    private String ipAddress;
    private Instant expiresAt;
    private Instant createdAt;
    private boolean revoked;
     */
}