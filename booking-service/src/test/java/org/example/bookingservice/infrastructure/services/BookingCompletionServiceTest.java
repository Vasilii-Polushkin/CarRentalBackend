package org.example.bookingservice.infrastructure.services;

import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.common.enums.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCompletionServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingStatusEventProducer bookingStatusEventProducer;

    @InjectMocks
    private BookingCompletionService bookingCompletionService;

    private final UUID TEST_BOOKING_ID = UUID.randomUUID();

    @Test
    void completeEndedRental_shouldUpdateStatusAndSendEvent() {
        Booking booking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .status(BookingStatus.RENTED)
                .build();

        Booking savedBooking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .status(BookingStatus.COMPLETED)
                .build();

        when(bookingRepository.save(booking)).thenReturn(savedBooking);

        bookingCompletionService.completeEndedRental(booking);

        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        verify(bookingStatusEventProducer).sendEvent(savedBooking);
    }

    @Test
    void completeEndedRental_shouldHaveTransactionalAnnotation() throws NoSuchMethodException {
        var method = BookingCompletionService.class.getDeclaredMethod("completeEndedRental", Booking.class);
        var transactionalAnnotation = method.getAnnotation(Transactional.class);

        assertNotNull(transactionalAnnotation);
        assertEquals(Propagation.REQUIRES_NEW, transactionalAnnotation.propagation());
    }

    @Test
    void completeEndedRental_shouldWorkWithDifferentStatuses() {
        Booking booking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .status(BookingStatus.PAID)
                .build();

        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingCompletionService.completeEndedRental(booking);

        assertEquals(BookingStatus.COMPLETED, booking.getStatus());
        verify(bookingStatusEventProducer).sendEvent(booking);
    }
}