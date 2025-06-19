package org.example.carservice.infrastructure.kafka.consumers;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.example.common.headers.CustomHeaders;
import org.example.common.topics.KafkaTopics;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import static org.example.common.correlation.CorrelationConstants.CORRELATION_ID_MDC;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final CarRepository carRepository;

    @KafkaListener(
            topics = KafkaTopics.PAYMENT_EVENTS,
            groupId = "car-service"
    )
    public void consumePaymentEvent(
            @Header(value = CustomHeaders.CORRELATION_ID_HEADER, required = false) String correlationId,
            @Payload PaymentEvent event
    ) {
        try {
            MDC.put(CORRELATION_ID_MDC, correlationId);
            log.info("Received payment event: {}", event);

            if (event.getStatus() == PaymentStatus.PAID) {
                Car car = carRepository
                        .findById(event.getCarId())
                        .orElseThrow(() -> new EntityNotFoundException("Car not found with id " + event.getCarId()));
                car.setStatus(CarStatus.RENTED);
                carRepository.save(car);
            } else {
                log.warn("Unhandled event status: {}", event.getStatus());
            }
        } catch (Exception e) {
            log.error("Error processing booking event: {}", event, e);
        }
        finally {
            MDC.remove(CORRELATION_ID_MDC);
        }
    }
}
