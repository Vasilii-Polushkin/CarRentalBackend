package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.userservice.api.dtos.UserDto;
import org.example.userservice.api.mappers.UserMapper;
import org.example.userservice.domain.services.UsersService;
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
    @RolesAllowed("ADMIN")
    public UserDto getUserById(@PathVariable("id") UUID id) {
        return userMapper.toDto(usersService.getUserById(id));
    }

    @DeleteMapping("{id}")
    @RolesAllowed("ADMIN")
    public void deleteUserById(@PathVariable("id") UUID id) {
        usersService.deleteUserById(id);
    }
}