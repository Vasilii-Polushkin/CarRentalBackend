package org.example.userservice.mappers;

import org.example.userservice.api.dtos.RegisterModelDto;
import org.example.userservice.domain.models.RegisterModel;
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
}