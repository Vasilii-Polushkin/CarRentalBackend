package org.example.carservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.example.common.dtos.CarCreateModelDto;
import org.example.common.dtos.CarDto;
import org.example.common.dtos.CarEditModelDto;
import org.example.carservice.api.mappers.CarCreteModelMapper;
import org.example.carservice.api.mappers.CarEditModelMapper;
import org.example.carservice.api.mappers.CarMapper;
import org.example.carservice.infrastructure.services.CarService;
import org.example.common.enums.CarStatus;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Cars")
public class CarsController {
    private final CarMapper carMapper;
    private final CarCreteModelMapper carCreteModelMapper;
    private final CarEditModelMapper carEditModelMapper;
    private final CarService carService;

    @PostMapping("cars")
    public CarDto createCar(@RequestBody CarCreateModelDto carCreateModel) {
        return carMapper.toDto(carService.createCar(carCreteModelMapper.toDomain(carCreateModel)));
    }

    @GetMapping("cars/{id}")
    public CarDto getCarById(@PathVariable("id") UUID id) {
        return carMapper.toDto(carService.getCarById(id));
    }

    @GetMapping("cars/available/{id}")
    public boolean isCarAvailableById(@PathVariable("id") UUID id) {
        return carService.isCarAvailable(id);
    }

    @PutMapping("cars/{id}/lock")
    public CarDto lockCarById(@PathVariable("id") UUID id) {
        return carMapper.toDto(carService.lockCarById(id));
    }

    @RolesAllowed("ADMIN")
    @GetMapping("cars")
    public List<CarDto> getAllCars() {
        return carService.getAllCars()
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') OR currentUserService.userId.equals(#id)")
    @GetMapping("user/{id}/cars")
    public List<CarDto> getAllCarsByOwnerId(@PathVariable("id") @Param("id") UUID id) {
        return carService.getAllCarsByUserId(id)
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @GetMapping("cars/available")
    public List<CarDto> getAvailableToRentalCars() {
        return carService.getAllAvailableToRentalCars()
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @DeleteMapping("cars/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @carAccessManager.isOwner(#id)")
    public void deleteCarById(@PathVariable("id") @Param("id") UUID id) {
        carService.deleteCarById(id);
    }

    @PutMapping("cars/{id}")
    @PreAuthorize("hasRole('ADMIN') OR @carAccessManager.isOwner(#id)")
    public CarDto editCarById(@PathVariable("id") @Param("id") UUID id, @RequestBody CarEditModelDto carEditModel) {
        return carMapper.toDto(
                carService.editCarById(id, carEditModelMapper.toDomain(carEditModel))
        );
    }

    @PutMapping("cars/{id}/onRepair/{isOnRepair}")
    @PreAuthorize("hasRole('ADMIN') OR @carAccessManager.isOwner(#id)")
    public CarDto changeCarRepairStatus(
            @PathVariable("id") @Param("id") UUID id,
            @PathVariable("isOnRepair") boolean isOnRepair
    ) {
        return carMapper.toDto(
                carService.changeCarRepairStatusById(id, isOnRepair)
        );
    }
}
