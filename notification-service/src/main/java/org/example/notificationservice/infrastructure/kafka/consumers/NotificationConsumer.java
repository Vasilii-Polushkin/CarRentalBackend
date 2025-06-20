package org.example.notificationservice.infrastructure.kafka.consumers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.events.BookingStatusEvent;
import org.example.common.events.PaymentEvent;
import org.example.common.topics.KafkaTopics;
import org.example.notificationservice.infrastructure.services.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopics.PAYMENT_EVENTS)
    public void handlePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {}", event);

        String message = String.format("New payment received: %.2f for booking %s",
                event.getAmount(), event.getBookingId());

        notificationService.sendNotification(event.getUserId(), message);
    }

    @KafkaListener(topics = KafkaTopics.BOOKING_STATUS_EVENTS)
    public void handleBookingStatusEvent(BookingStatusEvent event) {
        log.info("Received booking status event: {}", event);

        String message = "Booking " + event.getBookingId() + " " + event.getStatus().toString().toLowerCase();

        notificationService.sendNotification(event.getUserId(), message);
    }
}