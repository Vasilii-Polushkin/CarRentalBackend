package org.example.userservice.domain.models;

import lombok.Data;

@Data
public class OAuth2ProviderId {
    private String providerUserId;
    private String provider;
}