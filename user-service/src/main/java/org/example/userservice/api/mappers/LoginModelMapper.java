package org.example.userservice.api.mappers;

import org.example.userservice.api.dtos.LoginModelDto;
import org.example.userservice.domain.models.requests.LoginModel;
import org.springframework.stereotype.Component;

@Component
public class LoginModelMapper {
    public LoginModel toDomain(LoginModelDto model) {
        return new LoginModel(
                model.getEmail(),
                model.getPassword()
        );
    }
    public LoginModelDto toDto(LoginModel model) {
        return new LoginModelDto(
                model.getEmail(),
                model.getPassword()
        );
    }
}