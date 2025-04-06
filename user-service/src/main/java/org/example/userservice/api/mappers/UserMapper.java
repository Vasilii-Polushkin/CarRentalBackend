package org.example.userservice.api.mappers;

import org.example.userservice.api.dtos.UserDto;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.responses.UserCommonData;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User model) {
        return new UserDto(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getRoles()
        );
    }

    public UserDto toDto(UserCommonData model) {
        return new UserDto(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.getRoles()
        );
    }
}