package org.example.bookingservice.infrastructure.kafka.producer;

import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.common.enums.BookingStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.topics.KafkaTopics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusEventProducerTest {

    @Mock
    private KafkaTemplate<String, BookingStatusEvent> kafkaTemplate;

    @InjectMocks
    private BookingStatusEventProducer bookingStatusEventProducer;

    private final UUID TEST_BOOKING_ID = UUID.randomUUID();
    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(100.50);
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void sendEvent_shouldSendKafkaMessageWithCorrectHeaders() {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(TEST_BOOKING_ID)
                .carId(TEST_CAR_ID)
                .userId(TEST_USER_ID)
                .usdTotalAmount(TEST_AMOUNT)
                .status(BookingStatus.BOOKED)
                .timestamp(LocalDateTime.now())
                .build();

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));
        MDC.put("correlationId", TEST_CORRELATION_ID);

        bookingStatusEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(argThat((Message<BookingStatusEvent> message) ->
                Objects.equals(message.getHeaders().get(KafkaHeaders.TOPIC), KafkaTopics.BOOKING_STATUS_EVENTS) &&
                        Objects.equals(message.getHeaders().get(KafkaHeaders.KEY), TEST_BOOKING_ID.toString()) &&
                        message.getPayload().equals(event)
        ));
    }

    @Test
    void sendEventWithBooking_shouldBuildAndSendEvent() {
        Booking booking = Booking.builder()
                .id(TEST_BOOKING_ID)
                .carId(TEST_CAR_ID)
                .userId(TEST_USER_ID)
                .usdTotalAmount(TEST_AMOUNT)
                .status(BookingStatus.BOOKED)
                .build();

        when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

        bookingStatusEventProducer.sendEvent(booking);

        verify(kafkaTemplate).send(argThat((Message<BookingStatusEvent> message) ->
                message.getPayload().getBookingId().equals(TEST_BOOKING_ID) &&
                        message.getPayload().getCarId().equals(TEST_CAR_ID)
        ));
    }

    @Test
    void sendEvent_shouldHandleKafkaSendFailure() {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(TEST_BOOKING_ID)
                .build();

        CompletableFuture future = new CompletableFuture();
        future.completeExceptionally(new RuntimeException("Kafka error"));
        when(kafkaTemplate.send(any(Message.class))).thenReturn(future);

        bookingStatusEventProducer.sendEvent(event);

        verify(kafkaTemplate).send(any(Message.class));
    }
}