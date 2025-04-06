package org.example.userservice.domain.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@IdClass(OAuth2ProviderId.class)
public class OAuth2Provider {
    @Id
    private String providerUserId;

    @Id
    @NotBlank
    private String provider;

    @ManyToOne
    private User user;
}