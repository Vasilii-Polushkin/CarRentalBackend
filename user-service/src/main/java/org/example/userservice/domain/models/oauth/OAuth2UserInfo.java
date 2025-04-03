package org.example.userservice.domain.models.oauth;

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
}