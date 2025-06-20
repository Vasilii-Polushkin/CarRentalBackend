package org.example.carservice.infrastructure.kafka.consumers;

import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.CarStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private PaymentEventConsumer paymentEventConsumer;

    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void consumePaymentEvent_shouldUpdateCarStatusWhenPaid() {
        PaymentEvent event = PaymentEvent.builder()
                .carId(TEST_CAR_ID)
                .status(PaymentStatus.PAID)
                .build();

        Car car = new Car();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        assertEquals(CarStatus.RENTED, car.getStatus());
        verify(carRepository).save(car);
    }

    @Test
    void consumePaymentEvent_shouldNotUpdateCarWhenNotPaid() {
        PaymentEvent event = PaymentEvent.builder()
                .carId(TEST_CAR_ID)
                .status(PaymentStatus.PENDING)
                .build();

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        verify(carRepository, never()).findById(any());
        verify(carRepository, never()).save(any());
    }

    @Test
    void consumePaymentEvent_shouldHandleCarNotFound() {
        PaymentEvent event = PaymentEvent.builder()
                .carId(TEST_CAR_ID)
                .status(PaymentStatus.PAID)
                .build();

        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.empty());

        paymentEventConsumer.consumePaymentEvent(TEST_CORRELATION_ID, event);

        verify(carRepository, never()).save(any());
    }
}