package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.common.feign.clients.CarServiceClient;
import org.example.common.feign.clients.PaymentServiceClient;
import org.springframework.kafka.core.*;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.domain.models.requests.BookingCreateRequestModel;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;
import java.time.*;
import java.util.*;

@Service
@Validated
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final CarServiceClient carServiceClient;
    private final KafkaTemplate<String, BookingStatusEvent> kafkaTemplate;

    public Booking getBookingById(@NotNull UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id " + id));
    }

    @Transactional
    public Booking createBooking(@Valid BookingCreateRequestModel request) {
        if (!carServiceClient.isCarAvailable(request.getCarId())) {
            throw new BadRequestException("Car is not available");
        }

        Booking booking = Booking.builder()
                .carId(request.getCarId())
                .userId(request.getUserId())
                .startDate(LocalDateTime.now())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.BOOKED)
                .build();

        carServiceClient.changeCarStatus(request.getCarId(), CarStatus.BOOKED);

        sendBookingStatusEvent(booking);

        return bookingRepository.save(booking);
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    private void cancelExpiredBookings() {
        List<Booking> pending = bookingRepository
                .findByStatusAndCreatedAtBefore(
                        BookingStatus.BOOKED,
                        LocalDateTime.now().minusMinutes(30));

        pending.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            sendBookingStatusEvent(booking);
        });
    }

    //todo might be done better probably
    @Scheduled(fixedRate = 30 * 60 * 1000)
    private void completeExpiredRentals() {
        List<Booking> bookings = bookingRepository
                .findByStatusAndEndDateBefore(
                        BookingStatus.RENTED,
                        LocalDateTime.now()
                );

        bookings.forEach(booking -> {
            booking.setStatus(BookingStatus.COMPLETED);
            sendBookingStatusEvent(booking);
        });
    }

    private void sendBookingStatusEvent(@Valid Booking booking) {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(booking.getId())
                .carId(booking.getCarId())
                .userId(booking.getUserId())
                .status(booking.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("booking-status-topic", event);
    }
}