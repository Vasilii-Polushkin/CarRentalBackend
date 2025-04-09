package org.example.userservice.infrastructure.security.oauth;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            //todo throw custom exception
            throw new IllegalArgumentException("Login with " + registrationId + " is not supported");
        }
    }
}