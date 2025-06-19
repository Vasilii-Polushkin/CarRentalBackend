package org.example.paymentservice.infrastructure.kafka.consumers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.correlation.CorrelationConstants;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusEventConsumer {
    private final PaymentRepository paymentRepository;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "booking-service"
    )
    public void consumeBookingStatusEvent(
            @Header(value = CustomHeaders.CORRELATION_ID_HEADER, required = false) String correlationId,
            @Payload PaymentEvent event
    ) {
        try {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC, correlationId);
            log.info("Received booking status event: {}", event);

            if (event.getStatus() == PaymentStatus.CANCELED) {
                Payment payment = paymentRepository
                        .findById(event.getPaymentId())
                        .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + event.getPaymentId()));
                payment.setStatus(PaymentStatus.CANCELED);
                paymentRepository.save(payment);
            }

        } catch (Exception e) {
            log.error("Error processing booking status event: {}", event, e);
        }
        finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC);
        }
    }
}