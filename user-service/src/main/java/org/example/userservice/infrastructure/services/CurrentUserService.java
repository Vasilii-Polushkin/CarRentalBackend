package org.example.userservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.infrastructure.exceptions.AuthException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;
    private final JwtAccessTokenService accessTokenService;

    public User getUser() {
        return userRepository
                .findByEmail(getEmail())
                .orElseThrow(() -> new AuthException("Unauthorized"));
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
