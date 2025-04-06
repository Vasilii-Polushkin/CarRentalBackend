package org.example.userservice.infrastructure.services;

import lombok.RequiredArgsConstructor;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.domain.models.User;
import org.example.userservice.domain.exceptions.AuthException;
import org.example.userservice.api.mappers.RolesMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final JwtAccessTokenService accessTokenService;
    private final UserRepository userRepository;
    private final RolesMapper rolesMapper;

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new AuthException("Unauthorized"));
    }

    public Set<Role> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return rolesMapper.toRoles(authentication.getAuthorities());
    }
}
