package org.example.userservice.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.userservice.domain.models.JwtAuthentication;
import org.example.userservice.services.JwtAccessTokenService;
import org.example.userservice.services.JwtRefreshTokenService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends GenericFilterBean {

    private static final String AUTHORIZATION = "Authorization";
    private final JwtAccessTokenService accessTokenService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        final String token = getTokenFromRequest((HttpServletRequest) request);

        if (token != null && accessTokenService.validateToken(token)) {
            final JwtAuthentication jwtInfoToken = new JwtAuthentication();

            jwtInfoToken.setEmail(accessTokenService.extractEmail(token));
            jwtInfoToken.setAuthenticated(true);

            SecurityContextHolder.getContext().setAuthentication(jwtInfoToken);
        }

        chain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }
}