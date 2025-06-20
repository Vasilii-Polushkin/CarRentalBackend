package org.example.carservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.domain.models.requests.CarCreateRequestModel;
import org.example.carservice.domain.models.requests.CarEditRequestModel;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.CarStatus;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CarService carService;

    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_USER_NAME = "test_user";
    private final LocalDateTime TEST_DATE_TIME = LocalDateTime.now();

    @Test
    void createCar_shouldCreateNewCar() {
        CarCreateRequestModel request = new CarCreateRequestModel("Tesla Model S", BigDecimal.valueOf(50));
        Car expectedCar = Car.builder()
                .model("Tesla Model S")
                .usdPerHour(BigDecimal.valueOf(50))
                .creatorId(TEST_USER_ID)
                .creatorName(TEST_USER_NAME)
                .status(CarStatus.AVAILABLE)
                .build();

        when(currentUserService.getUserId()).thenReturn(TEST_USER_ID);
        when(currentUserService.getUserName()).thenReturn(TEST_USER_NAME);
        when(carRepository.save(any(Car.class))).thenReturn(expectedCar);

        Car result = carService.createCar(request);

        assertEquals("Tesla Model S", result.getModel());
        assertEquals(BigDecimal.valueOf(50), result.getUsdPerHour());
        assertEquals(CarStatus.AVAILABLE, result.getStatus());
        verify(carRepository).save(any(Car.class));
    }

    @Test
    void getCarById_shouldReturnCar() {
        Car expectedCar = createTestCar();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(expectedCar));

        Car result = carService.getCarById(TEST_CAR_ID);

        assertEquals(expectedCar, result);
        verify(carRepository).findById(TEST_CAR_ID);
    }

    @Test
    void getCarById_shouldThrowWhenNotFound() {
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> carService.getCarById(TEST_CAR_ID));
    }

    @Test
    void getAllCars_shouldReturnAllCars() {
        List<Car> expectedCars = List.of(createTestCar());
        when(carRepository.findAll()).thenReturn(expectedCars);

        List<Car> result = carService.getAllCars();

        assertEquals(expectedCars, result);
        verify(carRepository).findAll();
    }

    @Test
    void getAllCarsByUserId_shouldReturnUserCars() {
        List<Car> expectedCars = List.of(createTestCar());
        when(carRepository.getAllByCreatorId(TEST_USER_ID)).thenReturn(expectedCars);

        List<Car> result = carService.getAllCarsByUserId(TEST_USER_ID);

        assertEquals(expectedCars, result);
        verify(carRepository).getAllByCreatorId(TEST_USER_ID);
    }

    @Test
    void getAllAvailableToRentalCars_shouldReturnAvailableCars() {
        List<Car> expectedCars = List.of(createTestCar());
        when(carRepository.findAllByStatus(CarStatus.AVAILABLE)).thenReturn(expectedCars);

        List<Car> result = carService.getAllAvailableToRentalCars();

        assertEquals(expectedCars, result);
        verify(carRepository).findAllByStatus(CarStatus.AVAILABLE);
    }

    @Test
    void isCarAvailable_shouldReturnTrueWhenAvailable() {
        Car availableCar = createTestCar();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(availableCar));

        boolean result = carService.isCarAvailable(TEST_CAR_ID);

        assertTrue(result);
    }

    @Test
    void isCarAvailable_shouldReturnFalseWhenNotAvailable() {
        Car unavailableCar = createTestCar();
        unavailableCar.setStatus(CarStatus.RENTED);
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(unavailableCar));

        boolean result = carService.isCarAvailable(TEST_CAR_ID);

        assertFalse(result);
    }

    @Test
    void lockCarById_shouldLockCar() {
        when(carRepository.tryLockCar(eq(TEST_CAR_ID), any(LocalDateTime.class))).thenReturn(1);
        Car lockedCar = createTestCar();
        lockedCar.setStatus(CarStatus.LOCKED);
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(lockedCar));

        Car result = carService.lockCarById(TEST_CAR_ID);

        assertEquals(CarStatus.LOCKED, result.getStatus());
        verify(carRepository).tryLockCar(eq(TEST_CAR_ID), any(LocalDateTime.class));
    }

    @Test
    void lockCarById_shouldThrowWhenCannotLock() {
        when(carRepository.tryLockCar(eq(TEST_CAR_ID), any(LocalDateTime.class))).thenReturn(0);

        assertThrows(BadRequestException.class, () -> carService.lockCarById(TEST_CAR_ID));
    }

    @Test
    void releaseExpiredLocks_shouldReleaseExpiredLocks() {
        Car lockedCar = createTestCar();
        lockedCar.setLockedUntil(TEST_DATE_TIME.minusMinutes(1));
        lockedCar.setStatus(CarStatus.LOCKED);
        List<Car> lockedCars = List.of(lockedCar);

        when(carRepository.getAllByLockedUntilBefore(any(LocalDateTime.class))).thenReturn(lockedCars);
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        carService.releaseExpiredLocks();

        verify(carRepository).getAllByLockedUntilBefore(any(LocalDateTime.class));
        verify(carRepository).save(lockedCar);
        assertNull(lockedCar.getLockedUntil());
        assertEquals(CarStatus.AVAILABLE, lockedCar.getStatus());
    }

    @Test
    void editCarById_shouldEditCar() {
        Car car = createTestCar();
        CarEditRequestModel editRequest = new CarEditRequestModel("New Model", BigDecimal.valueOf(60));

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car result = carService.editCarById(TEST_CAR_ID, editRequest);

        assertEquals("New Model", result.getModel());
        assertEquals(BigDecimal.valueOf(60), result.getUsdPerHour());
        assertNotNull(result.getModificationDate());
    }

    @Test
    void editCarById_shouldThrowWhenNotEditable() {
        Car car = createTestCar();
        car.setStatus(CarStatus.RENTED);
        CarEditRequestModel editRequest = new CarEditRequestModel("New Model", BigDecimal.valueOf(60));

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        assertThrows(BadRequestException.class, () -> carService.editCarById(TEST_CAR_ID, editRequest));
    }

    @Test
    void changeCarRepairStatusById_shouldChangeToRepair() {
        Car car = createTestCar();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car result = carService.changeCarRepairStatusById(TEST_CAR_ID, true);

        assertEquals(CarStatus.UNDER_REPAIR, result.getStatus());
    }

    @Test
    void changeCarRepairStatusById_shouldChangeToAvailable() {
        Car car = createTestCar();
        car.setStatus(CarStatus.UNDER_REPAIR);
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Car result = carService.changeCarRepairStatusById(TEST_CAR_ID, false);

        assertEquals(CarStatus.AVAILABLE, result.getStatus());
    }

    @Test
    void deleteCarById_shouldDeleteCar() {
        Car car = createTestCar();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        carService.deleteCarById(TEST_CAR_ID);

        verify(carRepository).delete(car);
    }

    @Test
    void deleteCarById_shouldThrowWhenNotDeletable() {
        Car car = createTestCar();
        car.setStatus(CarStatus.RENTED);
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        assertThrows(BadRequestException.class, () -> carService.deleteCarById(TEST_CAR_ID));
    }

    private Car createTestCar() {
        return Car.builder()
                .id(TEST_CAR_ID)
                .model("Tesla Model 3")
                .usdPerHour(BigDecimal.valueOf(50))
                .creationDate(LocalDate.now())
                .creatorId(TEST_USER_ID)
                .creatorName(TEST_USER_NAME)
                .status(CarStatus.AVAILABLE)
                .build();
    }
}