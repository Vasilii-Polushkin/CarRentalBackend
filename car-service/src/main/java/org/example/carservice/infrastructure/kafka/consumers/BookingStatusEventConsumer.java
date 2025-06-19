package org.example.carservice.infrastructure.kafka.consumers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static org.example.common.correlation.CorrelationConstants.CORRELATION_ID_MDC;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusEventConsumer {
    private final CarRepository carRepository;

    @KafkaListener(
            topics = KafkaTopics.BOOKING_STATUS_EVENTS
    )
    public void consumeBookingStatusEvent(
            @Header(CustomHeaders.CORRELATION_ID_HEADER) String correlationId,
            @Payload BookingStatusEvent event
    ) {
        try {
            MDC.put(CORRELATION_ID_MDC, correlationId);
            log.info("Received payment event: {}", event);

            Car car = carRepository
                    .findById(event.getCarId())
                    .orElseThrow(() ->new EntityNotFoundException("Car not found with id " + event.getCarId()));

            car.setLockedUntil(null);

            switch (event.getStatus()){
                case BOOKED -> car.setStatus(CarStatus.BOOKED);
                case RENTED -> car.setStatus(CarStatus.RENTED);
                case CANCELLED, COMPLETED -> car.setStatus(CarStatus.AVAILABLE);
                default -> log.warn("Unhandled event status: {}", event.getStatus());
            }

            carRepository.save(car);
        } catch (Exception e) {
            log.error("Error processing booking event: {}", event, e);
        }
        finally {
            MDC.remove(CORRELATION_ID_MDC);
        }
    }
}