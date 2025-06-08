package org.example.carservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CarAccessManager {
    private final CarRepository carRepository;
    private final CurrentUserService currentUserService;

    public boolean isOwner(UUID carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + carId));

        return car.getCreatorId().equals(currentUserService.getUserId());
    }
}