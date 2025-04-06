package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.UserDto;
import org.example.userservice.api.mappers.UserMapper;
import org.example.userservice.infrastructure.services.CurrentUserService;
import org.example.userservice.infrastructure.services.UsersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UsersController {
    private final UserMapper userMapper;
    private final UsersService usersService;

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable UUID id) {
        return userMapper.toDto(usersService.getUserById(id));
    }
}