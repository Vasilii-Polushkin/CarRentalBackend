package org.example.carservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import lombok.RequiredArgsConstructor;
import org.example.carservice.api.dtos.CarCreateModelDto;
import org.example.carservice.api.dtos.CarDto;
import org.example.carservice.api.dtos.CarEditModelDto;
import org.example.carservice.api.mappers.CarCreteModelMapper;
import org.example.carservice.api.mappers.CarEditModelMapper;
import org.example.carservice.api.mappers.CarMapper;
import org.example.carservice.infrastructure.services.CarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("cars")
@RequiredArgsConstructor
@Tag(name = "Cars")
public class CarsController {
    private final CarMapper carMapper;
    private final CarCreteModelMapper carCreteModelMapper;
    private final CarEditModelMapper carEditModelMapper;
    private final CarService carService;

    @PostMapping
    public CarDto createCar(@RequestBody CarCreateModelDto carCreateModel) {
        return carMapper.toDto(carService.createCar(carCreteModelMapper.toDomain(carCreateModel)));
    }

    @GetMapping("{id}")
    public CarDto getCarById(@PathVariable("id") UUID id) {
        return carMapper.toDto(carService.getCarById(id));
    }

    @GetMapping("")
    public List<CarDto> getAllCars() {
        return carService.getAllCars()
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @GetMapping("available")
    public List<CarDto> getAvailableToRentalCars() {
        return carService.getAllAvailableToRentalCars()
                .stream()
                .map(carMapper::toDto)
                .toList();
    }

    @DeleteMapping("{id}")
    public void deleteCarById(@PathVariable("id") UUID id) {
        carService.deleteCarById(id);
    }

    @PutMapping("{id}")
    public CarDto editCarById(@PathVariable("id") UUID id, @RequestBody CarEditModelDto carEditModel) {
        return carMapper.toDto(
                carService.editCarById(id, carEditModelMapper.toDomain(carEditModel))
        );
    }

    @PutMapping("{id}/onRepair/{isOnRepair}")
    public CarDto setCarRepairStatus(
            @PathVariable("id") UUID id,
            @PathVariable("isOnRepair") boolean isOnRepair
    ) {
        return carMapper.toDto(
                carService.changeCarRepairStatusById(id, isOnRepair)
        );
    }
}
