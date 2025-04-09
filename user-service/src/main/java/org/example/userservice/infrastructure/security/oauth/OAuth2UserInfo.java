package org.example.userservice.infrastructure.security.oauth;

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
}