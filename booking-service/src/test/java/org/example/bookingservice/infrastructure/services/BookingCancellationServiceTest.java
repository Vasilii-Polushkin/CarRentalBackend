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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BookingCancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingStatusEventProducer bookingStatusEventProducer;

    @InjectMocks
    private BookingCancellationService bookingCancellationService;

    private final UUID TEST_BOOKING_ID = UUID.randomUUID();

    @Test
    void cancelExpiredBooking_shouldUpdateStatusAndSendEvent() {
        
        Booking booking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .status(BookingStatus.BOOKED)
                .build();

        Booking savedBooking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .status(BookingStatus.CANCELLED)
                .build();

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        
        bookingCancellationService.cancelExpiredBooking(booking);

        
        verify(bookingRepository).save(booking);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingStatusEventProducer).sendEvent(savedBooking);
    }

    @Test
    void cancelExpiredBooking_shouldHaveCorrectTransactionalAnnotation() throws NoSuchMethodException {
        
        var method = BookingCancellationService.class
                .getDeclaredMethod("cancelExpiredBooking", Booking.class);
        var transactionalAnnotation = method.getAnnotation(Transactional.class);

        
        assertNotNull(transactionalAnnotation, "Method should have @Transactional annotation");
        assertEquals(Propagation.REQUIRES_NEW, transactionalAnnotation.propagation(),
                "Transaction propagation should be REQUIRES_NEW");
    }
}