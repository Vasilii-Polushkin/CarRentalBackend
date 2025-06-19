package org.example.bookingservice.infrastructure.services;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.common.enums.BookingStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingCompletionService {
    private final BookingRepository bookingRepository;
    private final BookingStatusEventProducer bookingStatusEventProducer;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void completeEndedRental(@NotNull @Valid Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
        bookingStatusEventProducer.sendEvent(bookingRepository.save(booking));
        log.debug("Completed booking with id {}", booking.getId());
    }
}