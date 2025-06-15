package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.*;
import org.example.userservice.api.mappers.JwtModelMapper;
import org.example.userservice.api.mappers.LoginModelMapper;
import org.example.userservice.api.mappers.RegisterModelMapper;
import org.example.userservice.domain.services.AuthService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final LoginModelMapper loginMapper;
    private final RegisterModelMapper registerMapper;
    private final JwtModelMapper jwtModelMapper;

    @PostMapping("login")
    public JwtModelDto login(@Valid @RequestBody LoginModelDto request) {
        return jwtModelMapper.toDto(
                authService.login(loginMapper.toDomain(request))
        );
    }

    @GetMapping("validate")
    public void isTokenValid() {}

    @GetMapping("/oauth2/authorization/{providerId}")
    public void initiateOAuth2Login(
            @Parameter(description = "OAuth2 provider ID (google, github, etc.)", example = "google")
            @PathVariable("providerId") String providerId,
            HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/" + providerId);
    }

    @PostMapping("revoke")
    public void revoke(@Valid @RequestBody TokenRefreshModelDto request) {
        authService.revoke(request.getValue());
    }

    @PostMapping("register")
    public JwtModelDto register(@Valid @RequestBody RegisterModelDto request) {
        return jwtModelMapper.toDto(
                authService.register(registerMapper.toDomain(request))
        );
    }

    @PostMapping("refresh")
    public JwtModelDto refreshAndRotate(@Valid @RequestBody TokenRefreshModelDto request) {
        return jwtModelMapper.toDto(
                authService.refreshAndRotate(request.getValue())
        );
    }
}