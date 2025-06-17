package org.example.carservice.api.mappers;

import org.example.common.dtos.CarEditModelDto;
import org.example.carservice.domain.models.requests.CarEditRequestModel;
import org.springframework.stereotype.Component;

@Component
public class CarEditModelMapper {
    public CarEditRequestModel toDomain(CarEditModelDto model) {
        return CarEditRequestModel.builder()
                .model(model.getModel())
                .usdPerHour(model.getUsdPerHour())
                .build();
    }
}