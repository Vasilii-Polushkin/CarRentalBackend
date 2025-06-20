package org.example.paymentservice.infrastructure.kafka.consumers;

import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.example.common.topics.KafkaTopics;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusEventConsumerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private BookingStatusEventConsumer bookingStatusEventConsumer;

    private final UUID TEST_PAYMENT_ID = UUID.randomUUID();
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void consumeBookingStatusEvent_shouldUpdatePaymentStatusWhenCanceled() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .status(PaymentStatus.CANCELED)
                .build();

        Payment payment = new Payment();
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        assertEquals(PaymentStatus.CANCELED, payment.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void consumeBookingStatusEvent_shouldNotUpdatePaymentForNonCanceledStatus() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .status(PaymentStatus.PAID)
                .build();

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        verify(paymentRepository, never()).findById(any());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void consumeBookingStatusEvent_shouldHandlePaymentNotFound() {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(TEST_PAYMENT_ID)
                .status(PaymentStatus.CANCELED)
                .build();

        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.empty());

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        verify(paymentRepository, never()).save(any());
    }
}