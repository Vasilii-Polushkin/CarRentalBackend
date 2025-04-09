package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.api.dtos.LoginModelDto;
import org.example.userservice.api.dtos.RegisterModelDto;
import org.example.userservice.api.dtos.TokenRefreshModelDto;
import org.example.userservice.domain.enums.Role;
import org.example.userservice.api.mappers.LoginModelMapper;
import org.example.userservice.api.mappers.RegisterModelMapper;
import org.example.userservice.infrastructure.services.AuthService;
import org.example.userservice.infrastructure.services.CurrentUserService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final LoginModelMapper loginMapper;
    private final RegisterModelMapper registerMapper;

    @PostMapping("login")
    public JwtModelDto login(@Valid @RequestBody LoginModelDto request) {
        return authService.login(loginMapper.toDomain(request));
    }

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
        return authService.register(registerMapper.toDomain(request));
    }

    @PostMapping("refresh")
    public JwtModelDto refreshAndRotate(@Valid @RequestBody TokenRefreshModelDto request) {
        return authService.refreshAndRotate(request.getValue());
    }
}