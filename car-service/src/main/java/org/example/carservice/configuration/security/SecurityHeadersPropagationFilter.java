package org.example.carservice.configuration.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.example.headers.CustomHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class SecurityHeadersPropagationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String rolesHeader = request.getHeader(CustomHeaders.USER_ROLES_HEADER);
        String idHeader = request.getHeader(CustomHeaders.USER_ID_HEADER);

        if (rolesHeader != null) {
            response.addHeader(CustomHeaders.USER_ROLES_HEADER, rolesHeader);
        }
        if (idHeader != null) {
            response.addHeader(CustomHeaders.USER_ID_HEADER, idHeader);
        }

        chain.doFilter(request, response);
    }
}