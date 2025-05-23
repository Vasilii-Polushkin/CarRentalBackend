package org.example.userservice.infrastructure.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.api.mappers.RolesMapper;
import org.example.enums.Role;
import org.example.userservice.infrastructure.services.JwtAccessTokenUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private static final String ROLES_HEADER = "X-User-Roles";
    private static final String ID_HEADER = "X-User-Id";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private final RolesMapper rolesMapper;
    private final JwtAccessTokenUtil accessTokenService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws IOException, ServletException {
        String token = getTokenFromRequestOrNull(request);
        SetContextAuthenticationAndAddHeaderIfValid(token, response);
        chain.doFilter(request, response);
    }

    private void SetContextAuthenticationAndAddHeaderIfValid(String token, HttpServletResponse response) {
        if (token == null){
            return;
        }
        String error = accessTokenService.getValidationErrorMessageOrNull(token);
        if (error != null) {
            log.warn("Token is present, but not valid. Token: {}, cause: {}", token, error);
            return;
        }

        Set<Role> roles = accessTokenService.extractRoles(token);
        UUID id = accessTokenService.extractId(token);
        List<SimpleGrantedAuthority> authorities = rolesMapper.toAuthorities(roles);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(token, null, authorities);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        String rolesString = roles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        response.addHeader(ROLES_HEADER, rolesString);
        response.addHeader(ID_HEADER, id.toString());
    }

    private String getTokenFromRequestOrNull(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}