package org.example.userservice.api.mappers;

import org.example.userservice.api.dtos.UserDto;
import org.example.userservice.api.dtos.UserEditModelDto;
import org.example.userservice.domain.models.entities.User;
import org.example.userservice.domain.models.requests.UserEditModel;
import org.springframework.stereotype.Component;

@Component
public class UserEditModelMapper {
    public UserEditModelDto toDto(UserEditModel model) {
        return new UserEditModelDto(
                model.getName()
        );
    }
    public UserEditModel toDomain(UserEditModelDto model) {
        return new UserEditModel(
                model.getName()
        );
    }
}