package org.example.userservice.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.api.dtos.TokenRefreshModelDto;
import org.example.userservice.common.exceptions.ErrorResponse;
import org.example.userservice.infrastructure.security.filters.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.example.userservice.infrastructure.security.models.CarRentalOauth2User;
import org.example.userservice.infrastructure.security.services.CustomUserDetailsService;
import org.example.userservice.infrastructure.services.AuthService;
import org.example.userservice.infrastructure.services.JwtAccessTokenService;
import org.example.userservice.infrastructure.services.OidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled=true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final AuthService authService;
    private final OidcUserService oidcUserService;

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
                        .successHandler(jwtAuthenticationSuccessHandler(authService))
                        .failureHandler(jwtAuthenticationFailureHandler())
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler jwtAuthenticationSuccessHandler(AuthService authService) {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            CarRentalOauth2User principal = (CarRentalOauth2User) oauthToken.getPrincipal();

            JwtModelDto jwt = authService.createAndSaveJwtToken(principal.getUser());
            ObjectMapper mapper = new ObjectMapper();
            String jwtJson = mapper.writeValueAsString(jwt);

            response.setContentType("application/json");
            response.getWriter().write(jwtJson);
        };
    }

    @Bean
    public AuthenticationFailureHandler jwtAuthenticationFailureHandler() {
        return (request, response, exception) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());

            ErrorResponse errorResponse = new ErrorResponse("OAuth2 authentication failed");
            ObjectMapper mapper = new ObjectMapper();
            String errorResponseJson = mapper.writeValueAsString(errorResponse);

            response.setContentType("application/json");
            response.getWriter().write(errorResponseJson);
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
}