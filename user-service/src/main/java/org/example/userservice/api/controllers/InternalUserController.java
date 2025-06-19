package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.common.dtos.UserDto;
import org.example.common.headers.CustomHeaders;
import org.example.userservice.api.mappers.UserMapper;
import org.example.userservice.domain.services.UsersService;
import org.example.userservice.infrastructure.exceptions.AuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Tag(name = "Users Internal")
public class InternalUserController {

    private final UsersService usersService;
    private final UserMapper userMapper;
    @Value("${app.api.key}")
    private String apiKey;

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable("id") UUID id,
                               @RequestHeader(CustomHeaders.API_KEY_HEADER) String apiKeyHeaderValue) {
        if (!apiKey.equals(apiKeyHeaderValue)) {
            throw new AuthException("Invalid API key");
        }
        return userMapper.toDto(usersService.getUserById(id));
    }
}