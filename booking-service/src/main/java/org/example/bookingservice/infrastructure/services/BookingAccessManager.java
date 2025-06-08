package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingAccessManager {
    private final BookingRepository bookingRepository;
    private final CurrentUserService currentUserService;

    public boolean isOwner(UUID paymentId) {
        Booking booking = bookingRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + paymentId));

        return booking.getUserId().equals(currentUserService.getUserId());
    }
}