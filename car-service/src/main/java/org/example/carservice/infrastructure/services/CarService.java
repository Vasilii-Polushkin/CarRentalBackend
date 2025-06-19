package org.example.carservice.infrastructure.services;


import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.domain.models.requests.CarCreateRequestModel;
import org.example.carservice.domain.models.requests.CarEditRequestModel;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.CarStatus;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.common.exceptions.status_code_exceptions.InternalServerErrorException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class CarService {
    private final CarRepository carRepository;
    private final CurrentUserService currentUserService;

    public Car createCar(@NonNull @Valid CarCreateRequestModel carCreateModel) {
        Car car = Car.builder()
                .model(carCreateModel.getModel())
                .usdPerHour(carCreateModel.getUsdPerHour())
                .creationDate(LocalDate.now())
                .creatorId(currentUserService.getUserId())
                .creatorName(currentUserService.getUserName())
                .status(CarStatus.AVAILABLE)
                .build();

        Car savedCar = carRepository.save(car);
        log.info("Car created with model {} and id {}", car.getModel(), car.getId());
        return savedCar;
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

    public List<Car> getAllCarsByUserId(@NonNull UUID userId) {
        return carRepository
                .getAllByCreatorId(userId);
    }

    public List<Car> getAllAvailableToRentalCars() {
        return carRepository
                .findAllByStatus(CarStatus.AVAILABLE);
    }

    public boolean isCarAvailable(@NonNull UUID id) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        return car.getStatus() == CarStatus.AVAILABLE;
    }

    @Transactional
    public Car lockCarById(@NonNull UUID id) {
        int updated = carRepository.tryLockCar(
                id,
                LocalDateTime.now().plusMinutes(5)
        );

        if (updated == 0) {
            throw new BadRequestException("Car cannot be locked");
        }

        log.info("Car locked with id {}", id);

        return carRepository.findById(id)
                .orElseThrow(() -> new InternalServerErrorException("Car not found with id " + id));
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredLocks() {
        log.info("Releasing expired car locks...");
        List<Car> carsToUnlock = carRepository.getAllByLockedUntilBefore(LocalDateTime.now());
        log.debug("Found {} locked cars", carsToUnlock.size());
        carsToUnlock.forEach(car -> {
            car.setLockedUntil(null);
            car.setStatus(CarStatus.AVAILABLE);
            carRepository.save(car);
        });
        log.info("Released all expired car locks");
    }

    public Car editCarById(@NonNull UUID id, @Valid @NonNull CarEditRequestModel carEditModel) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        if (car.getStatus() != CarStatus.AVAILABLE && car.getStatus() != CarStatus.UNDER_REPAIR) {
            throw new BadRequestException("Car is currently not available to editing");
        }

        car.setModificationDate(LocalDate.now());
        car.setModel(carEditModel.getModel());
        car.setUsdPerHour(carEditModel.getUsdPerHour());

        Car savedCar = carRepository.save(car);
        log.info("Car edited with id {}", car.getId());
        return savedCar;
    }

    public Car changeCarRepairStatusById(@NonNull UUID id, boolean isOnRepair) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        if (car.getStatus() != CarStatus.AVAILABLE && car.getStatus() != CarStatus.UNDER_REPAIR) {
            throw new BadRequestException("Car is currently not available to editing");
        }

        car.setStatus(isOnRepair ? CarStatus.UNDER_REPAIR : CarStatus.AVAILABLE);

        Car savedCar = carRepository.save(car);
        log.info("Car with id {} status changed to {}", savedCar.getId(), savedCar.getStatus());
        return savedCar;
    }

    public void deleteCarById(@NonNull UUID id) {
        Car car = carRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + id));

        if (car.getStatus() != CarStatus.AVAILABLE && car.getStatus() != CarStatus.UNDER_REPAIR) {
            throw new BadRequestException("Car cannot be deleted currently");
        }

        //todo mb activeness
        carRepository.delete(car);
        log.info("Car deleted with id {}", car.getId());
    }
}