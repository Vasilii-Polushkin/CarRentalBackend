package org.example.userservice.infrastructure.security.access_checkers;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.example.userservice.domain.services.CurrentUserService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AccessChecker {
    private final CurrentUserService userService;

    public boolean isSelf(@NonNull UUID userId) {
        UUID currentUserId = userService.getId();
        return userId.equals(currentUserId);
    }
}