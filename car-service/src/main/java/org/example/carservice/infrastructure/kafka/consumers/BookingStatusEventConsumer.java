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
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingStatusEventConsumer {

    private static final String CORRELATION_ID_MDC = "correlationId";
    private final CarRepository carRepository;

    @KafkaListener(
            topics = "booking-status-events"
    )
    public void consumeBookingStatusEvent(
            @Header(CustomHeaders.CORRELATION_ID_HEADER) String correlationId,
            @Payload BookingStatusEvent event,
            Acknowledgment acknowledgment
    ) {
        try {
            MDC.put(CORRELATION_ID_MDC, correlationId);
            log.info("Received payment event: {}", event);

            Car car = carRepository
                    .findById(event.getCarId())
                    .orElseThrow(() ->new EntityNotFoundException("Car not found with id " + event.getCarId()));

            switch (event.getStatus()){
                case BOOKED -> {
                    car.setStatus(CarStatus.BOOKED);
                    return;
                }
                case RENTED -> {
                    car.setStatus(CarStatus.RENTED);
                    return;
                }
                case CANCELLED, COMPLETED -> {
                    car.setStatus(CarStatus.AVAILABLE);
                    return;
                }
                default -> {
                    log.warn("Unhandled event status: {}", event.getStatus());
                }
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing booking event: {}", event, e);
        }
        finally {
            MDC.remove(CORRELATION_ID_MDC);
        }
    }
}