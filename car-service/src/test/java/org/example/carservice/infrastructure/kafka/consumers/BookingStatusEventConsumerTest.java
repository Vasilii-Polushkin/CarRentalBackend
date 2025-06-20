package org.example.carservice.infrastructure.kafka.consumers;

import org.example.carservice.domain.models.entities.Car;
import org.example.carservice.infrastructure.repositories.CarRepository;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.events.BookingStatusEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingStatusEventConsumerTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private BookingStatusEventConsumer bookingStatusEventConsumer;

    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final String TEST_CORRELATION_ID = "test-correlation-id";

    @Test
    void consumeBookingStatusEvent_shouldHandleBookedStatus() {
        BookingStatusEvent event = createTestEvent(BookingStatus.BOOKED);
        Car car = new Car();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        assertNull(car.getLockedUntil());
        assertEquals(CarStatus.BOOKED, car.getStatus());
        verify(carRepository).save(car);
    }

    @Test
    void consumeBookingStatusEvent_shouldHandleRentedStatus() {
        BookingStatusEvent event = createTestEvent(BookingStatus.RENTED);
        Car car = new Car();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        assertEquals(CarStatus.RENTED, car.getStatus());
    }

    @Test
    void consumeBookingStatusEvent_shouldHandleCancelledStatus() {
        BookingStatusEvent event = createTestEvent(BookingStatus.CANCELLED);
        Car car = new Car();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        assertEquals(CarStatus.AVAILABLE, car.getStatus());
    }

    @Test
    void consumeBookingStatusEvent_shouldHandleCompletedStatus() {
        BookingStatusEvent event = createTestEvent(BookingStatus.COMPLETED);
        Car car = new Car();
        when(carRepository.findById(TEST_CAR_ID)).thenReturn(Optional.of(car));

        bookingStatusEventConsumer.consumeBookingStatusEvent(TEST_CORRELATION_ID, event);

        assertEquals(CarStatus.AVAILABLE, car.getStatus());
    }

    @Test
    void consumeBookingStatusEvent_shouldHaveCorrectAnnotations() throws NoSuchMethodException {
        var method = BookingStatusEventConsumer.class.getMethod(
                "consumeBookingStatusEvent",
                String.class,
                BookingStatusEvent.class
        );

        assertNotNull(method.getAnnotation(KafkaListener.class));
        assertNotNull(method.getParameters()[0].getAnnotation(Header.class));
        assertNotNull(method.getParameters()[1].getAnnotation(Payload.class));
    }

    private BookingStatusEvent createTestEvent(BookingStatus status) {
        return BookingStatusEvent.builder()
                .carId(TEST_CAR_ID)
                .status(status)
                .build();
    }
}