package org.example.userservice.intergration.api.controllers;

import org.example.userservice.UserServiceApplication;
import org.example.userservice.domain.services.AuthService;
import org.example.userservice.infrastructure.services.JwtAccessTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.example.userservice.api.controllers.UsersController;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.services.UsersService;
import org.example.userservice.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class UsersControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtAccessTokenUtil jwtAccessTokenUtil;

    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUpAdminUser(){
        adminUser = userRepository.save(
                User.builder()
                        .email("admin@admin.com")
                        .name("admin")
                        .roles(Set.of(Role.USER, Role.ADMIN))
                        .isActive(true)
                        .password("adminpass")
                        .build()
        );
        adminToken = jwtAccessTokenUtil.generateToken(adminUser);
    }

    @Test
    void getUser_ShouldReturnUser(){
        User savedUser = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .name("test")
                        .roles(Set.of(Role.USER))
                        .isActive(true)
                        .password("password")
                        .build()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        ResponseEntity<User> response = restTemplate.exchange(
                "/users/" + savedUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
    }
}