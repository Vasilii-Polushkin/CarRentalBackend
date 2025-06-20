package org.example.notificationservice.infrastructure.kafka.consumers;

import org.example.common.enums.BookingStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.events.PaymentEvent;
import org.example.notificationservice.infrastructure.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.KafkaListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    private final UUID TEST_BOOKING_ID = UUID.randomUUID();
    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(100.50);
    private final LocalDateTime TEST_TIMESTAMP = LocalDateTime.now();

    @Test
    void handlePaymentEvent_shouldProcessPaymentEvent() {
        PaymentEvent event = new PaymentEvent();
        event.setUserId(TEST_USER_ID);
        event.setBookingId(TEST_BOOKING_ID);
        event.setAmount(TEST_AMOUNT);

        String expectedMessage = String.format("New payment received: %.2f for booking %s",
                TEST_AMOUNT, TEST_BOOKING_ID);

        notificationConsumer.handlePaymentEvent(event);

        verify(notificationService).sendNotification(TEST_USER_ID, expectedMessage);
    }

    @Test
    void handleBookingStatusEvent_shouldProcessBookingStatusEvent() {
        BookingStatusEvent event = BookingStatusEvent.builder()
                .bookingId(TEST_BOOKING_ID)
                .carId(TEST_CAR_ID)
                .userId(TEST_USER_ID)
                .usdTotalAmount(TEST_AMOUNT)
                .status(BookingStatus.BOOKED)
                .timestamp(TEST_TIMESTAMP)
                .build();

        String expectedMessage = "Booking " + TEST_BOOKING_ID + " booked";

        notificationConsumer.handleBookingStatusEvent(event);

        verify(notificationService).sendNotification(TEST_USER_ID, expectedMessage);
    }
}