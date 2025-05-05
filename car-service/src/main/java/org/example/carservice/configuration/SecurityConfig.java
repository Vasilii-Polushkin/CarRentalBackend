package org.example.carservice.configuration;

import lombok.RequiredArgsConstructor;
import org.example.carservice.configuration.security.SecurityHeadersPropagationFilter;
import org.example.carservice.configuration.security.SetSecurityContextFromHeadersFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled=true)
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // TODO инжектить
                .addFilterBefore(new SetSecurityContextFromHeadersFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new SecurityHeadersPropagationFilter());

        return http.build();
    }
}