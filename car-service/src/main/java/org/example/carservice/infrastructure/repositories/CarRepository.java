package org.example.carservice.infrastructure.repositories;

import jakarta.validation.constraints.NotNull;
import org.example.carservice.domain.models.entities.Car;
import org.example.common.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findAllByStatus(@NotNull CarStatus status);
}