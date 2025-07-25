package org.example.bookingservice.infrastructure.kafka.consumers;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.bookingservice.infrastructure.services.BookingService;
import org.example.common.correlation.CorrelationConstants;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {
    private final BookingRepository bookingRepository;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "booking-service"
    )
    public void consumePaymentEvent(
            @Header(value = CustomHeaders.CORRELATION_ID_HEADER, required = false) String correlationId,
            @Payload PaymentEvent event
    ) {
        try {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC, correlationId);
            log.info("Received payment event: {}", event);

            if (event.getStatus() == PaymentStatus.PAID) {
                Booking booking = bookingRepository
                        .findById(event.getPaymentId())
                        .orElseThrow(() -> new EntityNotFoundException("Booking not found with id " + event.getPaymentId()));
                booking.setStatus(BookingStatus.RENTED);
                bookingRepository.save(booking);
            } else {
                log.warn("Unhandled event status: {}", event.getStatus());
            }

        } catch (Exception e) {
            log.error("Error processing payment event: {}", event, e);
        }
        finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC);
        }
    }
}