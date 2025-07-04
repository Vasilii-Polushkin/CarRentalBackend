package org.example.carservice.api.mappers;

import org.example.common.dtos.CarDto;
import org.example.carservice.domain.models.entities.Car;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {
    public CarDto toDto(Car model) {
        return CarDto.builder()
                .id(model.getId())
                .creatorId(model.getCreatorId())
                .creatorName(model.getCreatorName())
                .usdPerHour(model.getUsdPerHour())
                .status(model.getStatus())
                .model(model.getModel())
                .build();
    }
}