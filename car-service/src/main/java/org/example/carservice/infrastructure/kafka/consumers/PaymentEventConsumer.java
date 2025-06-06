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
import org.example.common.topics.KafkaTopics;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

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
            @Payload PaymentEvent event,
            Acknowledgment acknowledgment
    ) {
        try {
            log.info("Received payment event: {}", event);

            if (event.getStatus() == PaymentStatus.PAID) {
                Car car = carRepository
                        .findById(event.getCarId())
                        .orElseThrow(() ->new EntityNotFoundException("Car not found with id " + event.getCarId()));
                car.setStatus(CarStatus.RENTED);
            } else {
                log.warn("Unhandled event status: {}", event.getStatus());
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing payment event: {}", event, e);
            // todo dead-letter queue logic here
        }
    }
}
