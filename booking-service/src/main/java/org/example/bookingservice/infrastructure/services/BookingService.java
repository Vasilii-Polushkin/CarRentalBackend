package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.common.feign.clients.CarServiceClient;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.domain.models.requests.BookingCreateRequestModel;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;
import java.time.*;
import java.util.*;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CarServiceClient carServiceClient;
    private final CurrentUserService currentUserService;
    private final BookingStatusEventProducer bookingStatusEventProducer;

    public Booking getBookingById(@NotNull UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id " + id));
    }

    public List<Booking> getAllBookingsByCarId(@NotNull UUID id) {
        return bookingRepository.findAllByCarId(id);
    }

    public List<Booking> getAllBookingsByUserId(@NotNull UUID id) {
        return bookingRepository.findAllByUserId(id);
    }

    public List<Booking> getAllCurrentUsersBookings() {
        return bookingRepository.findAllByUserId(currentUserService.getUserId());
    }

    @Transactional
    public Booking createBooking(@Valid BookingCreateRequestModel request) {
        if (!carServiceClient.isCarAvailable(request.getCarId())) {
            throw new BadRequestException("Car is not available");
        }

        Booking booking = Booking.builder()
                .carId(request.getCarId())
                .userId(currentUserService.getUserId())
                .startDate(LocalDateTime.now())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.BOOKED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        sendBookingStatusEvent(savedBooking);
        log.info("Booking created with id {} for car with id {}", savedBooking.getId(), savedBooking.getCarId());
        return savedBooking;
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    private void cancelExpiredBookings() {
        log.info("Cancelling expired bookings...");
        List<Booking> pending = bookingRepository
                .findByStatusAndCreatedAtBefore(
                        BookingStatus.BOOKED,
                        LocalDateTime.now().minusMinutes(30));

        pending.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            sendBookingStatusEvent(bookingRepository.save(booking));
        });
        log.info("All expired bookings are cancelled");
    }

    //todo might be done better probably
    @Scheduled(fixedRate = 30 * 60 * 1000)
    private void completeEndedRentals() {
        log.info("Checking for bookings to complete...");
        List<Booking> bookings = bookingRepository
                .findByStatusAndEndDateBefore(
                        BookingStatus.RENTED,
                        LocalDateTime.now()
                );

        bookings.forEach(booking -> {
            booking.setStatus(BookingStatus.COMPLETED);
            sendBookingStatusEvent(bookingRepository.save(booking));
        });
        log.info("Bookings that should've been completed changed statuses and events has been send");
    }

    private void sendBookingStatusEvent(@Valid Booking booking) {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(booking.getId())
                .carId(booking.getCarId())
                .userId(booking.getUserId())
                .status(booking.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
        bookingStatusEventProducer.sendEvent(event);
    }
}