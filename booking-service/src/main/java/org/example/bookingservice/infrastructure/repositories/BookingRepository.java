package org.example.bookingservice.infrastructure.repositories;

import jakarta.validation.constraints.NotNull;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus bookingStatus, LocalDateTime localDateTime);
    List<Booking> findByStatusAndEndDateBefore(BookingStatus bookingStatus, LocalDateTime localDateTime);
}