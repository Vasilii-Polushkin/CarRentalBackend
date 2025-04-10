package org.example.userservice.infrastructure.security.oauth.user_info;

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
}