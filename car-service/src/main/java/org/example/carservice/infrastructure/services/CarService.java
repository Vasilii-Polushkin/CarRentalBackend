package org.example.carservice.infrastructure.services;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.domain.models.requests.CarCreateRequestModel;
import org.example.carservice.domain.models.requests.CarEditRequestModel;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class CarService {
    private final CarRepository carRepository;
    private final CurrentUserService currentUserService;

    public Car createCar(CarCreateRequestModel carCreateModel) {
        Car car = Car.builder()
                .model(carCreateModel.getModel())
                .creationDate(LocalDate.now())
                .creatorId(currentUserService.getUserId())
                .creatorName(currentUserService.getUserName())
                .isOnRental(false)
                .isOnRepair(false)
                .build();

        return carRepository.save(car);
    }

    public Car getCarById(@NonNull UUID id) {
        return carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));
    }

    public List<Car> getAllCars() {
        return carRepository
                .findAll();
    }

    public List<Car> getAllAvailableToRentalCars() {
        return carRepository
                .findAllByIsOnRentalIsFalse();
    }

    public Car editCarById(@NonNull UUID id, @Valid @NonNull CarEditRequestModel carEditModel) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        car.setModificationDate(LocalDate.now());
        car.setOnRepair(carEditModel.isOnRepair());
        car.setOnRental(carEditModel.isOnRental());
        car.setModel(carEditModel.getModel());

        return carRepository.save(car);
    }

    public Car changeCarRepairStatusById(@NonNull UUID id, boolean isOnRepair) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        car.setOnRepair(isOnRepair);

        return carRepository.save(car);
    }

    public void deleteCarById(@NonNull UUID id) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id));

        carRepository.delete(car);
    }
}