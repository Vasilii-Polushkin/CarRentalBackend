package org.example.userservice.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.api.mappers.JwtModelMapper;
import org.example.userservice.common.exceptions.ErrorResponse;
import org.example.userservice.domain.models.responses.JwtModel;
import org.example.userservice.infrastructure.security.filters.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.example.userservice.infrastructure.security.oauth.CustomOauth2User;
import org.example.userservice.infrastructure.services.AuthService;
import org.example.userservice.infrastructure.security.oauth.OAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled=true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthService authService;
    private final OAuth2UserService oidcUserService;
    private final JwtModelMapper jwtModelMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register" ,
                                "/swagger-ui/**",
                                "/swagger-resources/*",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/auth/oauth2/**",
                                "/oauth2/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oidcUserService)
                        )
                        .successHandler(jwtAuthenticationSuccessHandler(authService, jwtModelMapper))
                        .failureHandler(jwtAuthenticationFailureHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(
                                oauth2AuthenticationEntryPoint()
                        )
                );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint oauth2AuthenticationEntryPoint() {
        return (request, response, authException) -> {
            WriteAuthErrorToHttpResponse(response, new ErrorResponse(authException.getMessage()));
        };
    }

    @Bean
    public AuthenticationSuccessHandler jwtAuthenticationSuccessHandler(AuthService authService, JwtModelMapper jwtModelMapper) {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            CustomOauth2User principal = (CustomOauth2User) oauthToken.getPrincipal();
            JwtModel jwt = authService.createAndSaveJwtToken(principal.getUser());

            WriteObjectToHttpResponse(response, jwtModelMapper.toDto(jwt));
        };
    }

    @Bean
    public AuthenticationFailureHandler jwtAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            WriteAuthErrorToHttpResponse(response, new ErrorResponse("OAuth2 authentication failed"));
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }

    private static void WriteAuthErrorToHttpResponse(HttpServletResponse response, ErrorResponse errorResponse) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        WriteObjectToHttpResponse(response, errorResponse);
    }

    private static void WriteObjectToHttpResponse(HttpServletResponse response, Object object) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(object));
    }
}