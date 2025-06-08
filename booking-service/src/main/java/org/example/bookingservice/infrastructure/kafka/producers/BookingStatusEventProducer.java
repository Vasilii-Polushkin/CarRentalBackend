package org.example.bookingservice.infrastructure.kafka.producers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.correlation.CorrelationConstants;
import org.example.common.events.BookingStatusEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusEventProducer {

    private final KafkaTemplate<String, BookingStatusEvent> kafkaTemplate;

    public void sendEvent(BookingStatusEvent event) {
        try {
            Message<BookingStatusEvent> message = MessageBuilder
                    .withPayload(event)
                    .setHeader(KafkaHeaders.TOPIC, KafkaTopics.BOOKING_STATUS_EVENTS)
                    .setHeader(KafkaHeaders.KEY, event.getBookingId().toString())
                    .setHeader(CustomHeaders.CORRELATION_ID_HEADER, MDC.get(CorrelationConstants.CORRELATION_ID_MDC))
                    .build();

            kafkaTemplate.send(message)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Sent booking status event: {}", event);
                        } else {
                            log.error("Failed to send booking status event: {}", event, ex);
                        }
                    });
        } catch (Exception e) {
            log.error("Error sending booking status event: {}", event, e);
        }
    }
}