package org.example.carservice.api.mappers;

import org.example.carservice.api.dtos.CarEditModelDto;
import org.example.carservice.domain.models.requests.CarEditRequestModel;
import org.springframework.stereotype.Component;

@Component
public class CarEditModelMapper {
    public CarEditRequestModel toDomain(CarEditModelDto model) {
        return CarEditRequestModel.builder()
                .model(model.getModel())
                .isOnRental(model.isOnRental())
                .isOnRepair(model.isOnRepair())
                .build();
    }
}