package org.example.userservice.domain.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class OAuth2Provider {
    private String provider;

    @Id
    private String providerUserId;

    @ManyToOne
    private User user;
}