package org.example.userservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.example.userservice.domain.models.responses.UserCommonData;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.infrastructure.exceptions.AuthException;
import org.example.userservice.api.mappers.RolesMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final RolesMapper rolesMapper;
    private final JwtAccessTokenService accessTokenService;

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AuthException("Unauthorized"));
    }

    public UserCommonData getUserCommonData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new UserCommonData(
                getId(),
                getName(),
                getEmail(),
                getRoles()
        );
    }

    public Set<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return rolesMapper.toRoles(authentication.getAuthorities());
    }

    public String getEmail() {
        return accessTokenService.extractEmail(getToken());
    }

    public String getName() {
        return accessTokenService.extractName(getToken());
    }

    public UUID getId() {
        return accessTokenService.extractId(getToken());
    }

    private String getToken(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof String jwt) {
            return jwt;
        }

        throw new IllegalStateException("Invalid authentication type");
    }
}
