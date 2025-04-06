package org.example.userservice.api.mappers;

import org.example.userservice.api.dtos.RegisterModelDto;
import org.example.userservice.domain.models.requests.RegisterModel;
import org.springframework.stereotype.Component;

@Component
public class RegisterModelMapper {
    public RegisterModel toDomain(RegisterModelDto model) {
        return new RegisterModel(
                model.getName(),
                model.getEmail(),
                model.getPassword()
        );
    }
    public RegisterModelDto toDto(RegisterModel model) {
        return new RegisterModelDto(
                model.getName(),
                model.getEmail(),
                model.getPassword()
        );
    }
}