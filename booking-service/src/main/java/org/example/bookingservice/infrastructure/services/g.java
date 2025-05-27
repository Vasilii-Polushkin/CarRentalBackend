package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.bookingservice.api.dtos.BookingDto;
import org.example.bookingservice.api.mappers.BookingCreateModelMapper;
import org.example.bookingservice.api.mappers.BookingMapper;
import org.example.common.enums.BookingStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
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
    private final BookingMapper bookingMapper;
    private final BookingCreateModelMapper bookingCreateModelMapper;
    private final BookingRepository bookingRepository;
    private final CarServiceClient carServiceClient;
    private final PaymentServiceClient paymentServiceClient;
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
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.PENDING)
                .build();

        carServiceClient.updateCarStatus(request.getCarId(), "BOOKED");

        sendBookingStatusEvent(booking, "BOOKING_CREATED");

        return bookingRepository.save(booking);
    }

    @Transactional
    public void processPayment(@NotNull UUID bookingId, @Valid PaymentResponse paymentResponse) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException());

        if (paymentResponse.isSuccess()) {
            booking.setStatus(BookingStatus.RENTED);
            booking.setPaymentId(paymentResponse.getPaymentId());
            carServiceClient.updateCarStatus(booking.getCarId(), "RENTED");
            sendBookingStatusEvent(booking, "BOOKING_CONFIRMED");
        } else {
            cancelBooking(bookingId);
        }
    }

    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void cancelExpiredBookings() {
        List<Booking> pending = bookingRepository
                .findByStatusAndCreatedAtBefore(
                        BookingStatus.PENDING,
                        LocalDateTime.now().minusMinutes(30));

        pending.forEach(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            carServiceClient.updateCarStatus(booking.getCarId(), "AVAILABLE");
            sendBookingStatusEvent(booking, "BOOKING_CANCELLED");
        });
    }

    private void sendBookingStatusEvent(@Valid Booking booking, String eventType) {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(booking.getId())
                .carId(booking.getCarId())
                .userId(booking.getUserId())
                .status(booking.getStatus())
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .build();
        kafkaTemplate.send("booking-status-topic", event);
    }
}
