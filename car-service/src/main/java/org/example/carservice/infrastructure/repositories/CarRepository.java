package org.example.carservice.infrastructure.repositories;

import org.example.carservice.domain.models.entities.Car;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findAllByIsOnRentalIsFalse();
}