package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.domain.models.LoginRequestModel;
import org.example.userservice.domain.models.RefreshTokenResponse;
import org.example.userservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("login")
    public ResponseEntity<JwtModelDto> login(@RequestBody LoginRequestModel authRequest) {
        final JwtModelDto token = authService.login(authRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("refresh")
    public ResponseEntity<JwtModelDto> refreshAndRotate(@RequestBody RefreshTokenResponse request) {
        final JwtModelDto token = authService.refreshAndRotate(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }
}