package org.example.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.common.dtos.UserDto;
import org.example.userservice.api.dtos.*;
import org.example.userservice.api.mappers.UserEditModelMapper;
import org.example.userservice.api.mappers.UserMapper;
import org.example.userservice.domain.services.CurrentUserService;
import org.example.userservice.domain.services.UsersService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class CurrentUserController {
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;
    private final UserEditModelMapper userEditModelMapper;
    private final UsersService usersService;

    @GetMapping()
    public UserDto getUser() {
        return userMapper.toDto(currentUserService.getUser());
    }

    @PutMapping("edit")
    public void editUser(@Valid @RequestBody UserEditModelDto model) {
        usersService.editUserById(currentUserService.getId(), userEditModelMapper.toDomain(model));
    }
}