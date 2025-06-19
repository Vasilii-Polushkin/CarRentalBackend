package org.example.carservice.infrastructure.repositories;

import jakarta.validation.constraints.NotNull;
import org.example.carservice.domain.models.entities.Car;
import org.example.common.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CarRepository extends JpaRepository<Car, UUID> {
    List<Car> findAllByStatus(@NotNull CarStatus status);

    List<Car> getAllByLockedUntilBefore(LocalDateTime lockedUntilBefore);

    @Modifying
    @Query("UPDATE Car c SET c.status = 'LOCKED', c.lockedUntil = :lockedUntil " +
            "WHERE c.id = :id AND c.status = 'AVAILABLE' AND c.lockedUntil IS NULL")
    int tryLockCar(@Param("id") UUID id,
                   @Param("lockedUntil") LocalDateTime lockedUntil);
}