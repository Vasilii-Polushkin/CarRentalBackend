package org.example.userservice.domain.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.example.userservice.domain.models.entities.ids.OAuth2ProviderId;

@Entity
@Data
@IdClass(OAuth2ProviderId.class)
public class OAuth2Provider {
    @Id
    private String providerUserId;

    @Id
    private String provider;

    @ManyToOne
    private User user;
}