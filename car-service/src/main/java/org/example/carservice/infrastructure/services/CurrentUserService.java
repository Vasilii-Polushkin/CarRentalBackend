package org.example.carservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.example.common.exceptions.status_code_exceptions.UnauthorizedException;
import org.example.common.feign.clients.UserServiceClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserServiceClient userServiceClient;

    public UUID getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication.getPrincipal() instanceof String id) {
            try {
                return UUID.fromString(id);
            }
            catch (Exception e) {
                throw new UnauthorizedException("Invalid user id");
            }
        }

        throw new IllegalStateException("Invalid authentication type");
    }

    public String getUserName() {
        UUID userId = getUserId();
        return userServiceClient.getUserById(userId).getName();
    }
}