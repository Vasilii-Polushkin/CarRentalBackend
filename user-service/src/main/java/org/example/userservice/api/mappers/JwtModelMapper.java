package org.example.userservice.api.mappers;

import org.example.userservice.api.dtos.JwtModelDto;
import org.example.userservice.domain.models.responses.JwtModel;
import org.springframework.stereotype.Component;

@Component
public class JwtModelMapper {
    public JwtModel toDomain(JwtModelDto model) {
        return new JwtModel(
                model.getAccessToken(),
                model.getRefreshToken()
        );
    }
    public JwtModelDto toDto(JwtModel model) {
        return new JwtModelDto(
                model.getAccessToken(),
                model.getRefreshToken()
        );
    }
}
