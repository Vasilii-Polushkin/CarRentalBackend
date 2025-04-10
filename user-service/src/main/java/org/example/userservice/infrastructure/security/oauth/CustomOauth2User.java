package org.example.userservice.infrastructure.security.oauth;

import lombok.Getter;
import org.example.userservice.api.mappers.RolesMapper;
import org.example.userservice.domain.models.entities.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOauth2User implements OAuth2User {
    private final Map<String, Object> attributes;
    private final RolesMapper rolesMapper;
    @Getter
    private final User user;

    public CustomOauth2User(User user, Map<String, Object> attributes, RolesMapper rolesMapper) {
        this.attributes = attributes;
        this.user = user;
        this.rolesMapper = rolesMapper;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return rolesMapper.toAuthorities(user.getRoles());
    }

    @Override
    public String getName() {
        return user.getEmail();
    }
}