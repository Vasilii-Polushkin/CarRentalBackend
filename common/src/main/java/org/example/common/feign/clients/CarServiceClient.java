package org.example.common.feign.clients;

import org.example.common.dtos.CarCreateModelDto;
import org.example.common.dtos.CarDto;
import org.example.common.dtos.CarEditModelDto;
import org.example.common.enums.CarStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "car-service",
        url = "http://localhost:8001/api/car-service"
)
public interface CarServiceClient {

    @PostMapping("/cars")
    CarDto createCar(@RequestBody CarCreateModelDto carCreateModel);

    @GetMapping("/cars/{id}")
    CarDto getCarById(@PathVariable("id") UUID id);

    @PutMapping("/cars/{id}/lock")
    CarDto lockCarById(@PathVariable("id") UUID id);

    @GetMapping("/cars/available/{id}")
    boolean isCarAvailable(@PathVariable("id") UUID id);

    @GetMapping("/cars")
    List<CarDto> getAllCars();

    @GetMapping("/cars/available")
    List<CarDto> getAvailableToRentalCars();

    @DeleteMapping("/cars/{id}")
    void deleteCarById(@PathVariable("id") UUID id);

    @PutMapping("/cars/{id}")
    CarDto editCarById(@PathVariable("id") UUID id, @RequestBody CarEditModelDto carEditModel);

    @PutMapping("/cars/{id}/onRepair/{isOnRepair}")
    CarDto changeCarRepairStatus(
            @PathVariable("id") UUID id,
            @PathVariable("isOnRepair") boolean isOnRepair
    );

    @PutMapping("/cars/{id}/status/{status}")
    CarDto changeCarStatus(
            @PathVariable("id") UUID id,
            @PathVariable("status") CarStatus status
    );
}