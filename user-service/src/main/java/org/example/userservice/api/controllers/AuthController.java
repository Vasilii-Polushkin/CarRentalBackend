package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.util.List;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;
    private final LoginModelMapper loginMapper;
    private final RegisterModelMapper registerMapper;
    private final CurrentUserService currentUserService;

    @PostMapping("login")
    public JwtModelDto login(@RequestBody LoginModelDto request) {
        return authService.login(loginMapper.toDomain(request));
    }

    @PostMapping("register")
    public JwtModelDto register(@RequestBody RegisterModelDto request) {
        return authService.register(registerMapper.toDomain(request));
    }

    @PostMapping("refresh")
    public JwtModelDto refreshAndRotate(@RequestBody TokenRefreshModelDto request) {
        return authService.refreshAndRotate(request.getValue());
    }

    @GetMapping("test")
    public String test() {
        return "Hi";
    }

    @GetMapping("test/roles")
    public List<Role> testRoles() {
        return currentUserService.getRoles().stream().toList();
    }
}