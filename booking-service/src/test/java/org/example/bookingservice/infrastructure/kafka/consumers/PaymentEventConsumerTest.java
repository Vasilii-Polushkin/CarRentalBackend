package org.example.bookingservice.infrastructure.kafka.consumers;

import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.example.common.topics.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private final UUID TEST_PAYMENT_ID = UUID.randomUUID();
    private final UUID TEST_BOOKING_ID = UUID.randomUUID();
    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void consumePaymentEvent_shouldUpdateBookingStatusWhenPaid() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .bookingId(TEST_BOOKING_ID)
                .carId(TEST_CAR_ID)
                .userId(TEST_USER_ID)
                .status(PaymentStatus.PAID)
                .build();

        Booking booking = new Booking();
        when(bookingRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(booking));

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        assertEquals(BookingStatus.RENTED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void consumePaymentEvent_shouldNotUpdateBookingWhenNotPaid() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .status(PaymentStatus.PENDING)
                .build();

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        verifyNoInteractions(bookingRepository);
    }

    @Test
    void consumePaymentEvent_shouldHandleBookingNotFound() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .status(PaymentStatus.PAID)
                .build();

        when(bookingRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        verify(bookingRepository, never()).save(any());
    }
}