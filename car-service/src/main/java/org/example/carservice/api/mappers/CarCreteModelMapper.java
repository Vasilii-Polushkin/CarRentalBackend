package org.example.carservice.api.mappers;

import org.example.common.dtos.CarCreateModelDto;
import org.example.carservice.domain.models.requests.CarCreateRequestModel;
import org.springframework.stereotype.Component;

@Component
public class CarCreteModelMapper {
    public CarCreateRequestModel toDomain(CarCreateModelDto model) {
        return CarCreateRequestModel.builder()
                .model(model.getModel())
                .usdPerHour(model.getUsdPerHour())
                .build();
    }
}