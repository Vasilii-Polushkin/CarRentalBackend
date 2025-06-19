package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.common.dtos.CarDto;
import org.example.common.dtos.PaymentCreateModelDto;
import org.example.common.dtos.PaymentDto;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.common.feign.clients.CarServiceClient;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.domain.models.requests.BookingCreateRequestModel;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.common.feign.clients.PaymentServiceClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.springframework.validation.annotation.*;

import java.math.BigDecimal;
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
    private final PaymentServiceClient paymentServiceClient;

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
    public Booking cancelBooking(@NotNull UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id " + id));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        sendBookingStatusEvent(savedBooking);
        log.info("Booking cancelled with id {}", id);
        return savedBooking;
    }

    @Transactional
    public Booking createBooking(@NotNull @Valid BookingCreateRequestModel request) {
        CarDto car = carServiceClient.lockCarById(request.getCarId());

        if (!car.getStatus().equals(CarStatus.AVAILABLE)) {
            throw new BadRequestException("Car is not available");
        }

        BigDecimal hoursOfRental = BigDecimal.valueOf(
                (Duration.between(LocalDateTime.now(), request.getEndDate()).toMinutes() + 1) * 60
        );
        BigDecimal totalAmount = hoursOfRental.multiply(car.getUsdPerHour());

        Booking booking = Booking.builder()
                .carId(request.getCarId())
                .userId(currentUserService.getUserId())
                .usdTotalAmount(totalAmount)
                .startDate(LocalDateTime.now())
                .endDate(request.getEndDate())
                .createdAt(LocalDateTime.now())
                .status(BookingStatus.BOOKED)
                .build();

        PaymentCreateModelDto paymentCreateModel = PaymentCreateModelDto.builder()
                .bookingId(booking.getId())
                .carId(car.getId())
                .usdTotalAmount(totalAmount)
                .build();

        PaymentDto payment = paymentServiceClient.createPayment(paymentCreateModel);
        booking.setPaymentId(payment.getId());
        Booking savedBooking = bookingRepository.save(booking);

        sendBookingStatusEvent(savedBooking);
        log.info("Booking created with id {} for car with id {}", savedBooking.getId(), savedBooking.getCarId());
        return savedBooking;
    }

    @Scheduled(fixedRate = 60 * 1000)
    private void cancelExpiredBookings() {
        log.info("Cancelling expired bookings...");
        List<Booking> pending = bookingRepository
                .findByStatusAndCreatedAtBefore(
                        BookingStatus.BOOKED,
                        LocalDateTime.now().minusMinutes(30));

        pending.forEach(this::cancelExpiredBooking);
        log.info("All expired bookings are cancelled and events has been send");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelExpiredBooking(@NotNull @Valid Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
        sendBookingStatusEvent(bookingRepository.save(booking));
        log.debug("Cancelled expired booking with id {}", booking.getId());
    }

    @Scheduled(fixedRate = 60 * 1000)
    private void completeEndedRentals() {
        log.info("Completing ended rentals...");
        List<Booking> bookings = bookingRepository
                .findByStatusAndEndDateBefore(
                        BookingStatus.RENTED,
                        LocalDateTime.now()
                );

        bookings.forEach(this::completeEndedRental);
        log.info("Bookings that should've been completed changed statuses and events has been send");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void completeEndedRental(@NotNull @Valid Booking booking) {
        booking.setStatus(BookingStatus.COMPLETED);
        sendBookingStatusEvent(bookingRepository.save(booking));
        log.debug("Completed booking with id {}", booking.getId());
    }

    private void sendBookingStatusEvent(@Valid Booking booking) {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(booking.getId())
                .carId(booking.getCarId())
                .userId(booking.getUserId())
                .usdTotalAmount(booking.getUsdTotalAmount())
                .status(booking.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
        bookingStatusEventProducer.sendEvent(event);
    }
}