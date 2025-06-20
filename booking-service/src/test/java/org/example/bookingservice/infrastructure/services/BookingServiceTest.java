package org.example.bookingservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import org.example.bookingservice.domain.models.entities.Booking;
import org.example.bookingservice.domain.models.requests.BookingCreateRequestModel;
import org.example.bookingservice.infrastructure.kafka.producers.BookingStatusEventProducer;
import org.example.bookingservice.infrastructure.repositories.BookingRepository;
import org.example.common.dtos.CarDto;
import org.example.common.dtos.PaymentCreateModelDto;
import org.example.common.dtos.PaymentDto;
import org.example.common.enums.BookingStatus;
import org.example.common.enums.CarStatus;
import org.example.common.enums.PaymentStatus;
import org.example.common.feign.clients.CarServiceClient;
import org.example.common.feign.clients.PaymentServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarServiceClient carServiceClient;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private BookingStatusEventProducer bookingStatusEventProducer;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @Mock
    private BookingCancellationService bookingCancellationService;

    @Mock
    private BookingCompletionService bookingCompletionService;

    @InjectMocks
    private BookingService bookingService;
    
    private final UUID TEST_BOOKING_ID = UUID.randomUUID();
    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final UUID TEST_PAYMENT_ID = UUID.randomUUID();
    private final UUID TEST_CREATOR_ID = UUID.randomUUID();
    private final UUID TEST_CARD_ID = UUID.randomUUID();
    private final LocalDateTime TEST_DATE = LocalDateTime.now();
    private final LocalDateTime FUTURE_DATE = LocalDateTime.now().plusHours(2);

    @Test
    void getBookingById_shouldReturnBooking_whenExists() {
        Booking expectedBooking = createTestBooking();
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(expectedBooking));

        
        Booking result = bookingService.getBookingById(TEST_BOOKING_ID);

        
        assertEquals(expectedBooking, result);
        verify(bookingRepository).findById(TEST_BOOKING_ID);
    }

    @Test
    void getBookingById_shouldThrowException_whenBookingNotFound() {
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.empty());


        assertThrows(EntityNotFoundException.class, () ->
                bookingService.getBookingById(TEST_BOOKING_ID));
        verify(bookingRepository).findById(TEST_BOOKING_ID);
        verifyNoInteractions(bookingStatusEventProducer);
    }

    @Test
    void getAllBookingsByCarId_shouldReturnListOfBookings() {
        List<Booking> expectedBookings = List.of(createTestBooking());
        when(bookingRepository.findAllByCarId(TEST_CAR_ID)).thenReturn(expectedBookings);

        
        List<Booking> result = bookingService.getAllBookingsByCarId(TEST_CAR_ID);

        
        assertEquals(expectedBookings, result);
        verify(bookingRepository).findAllByCarId(TEST_CAR_ID);
    }

    @Test
    void getAllBookingsByUserId_shouldReturnListOfBookings() {
        List<Booking> expectedBookings = List.of(createTestBooking());
        when(bookingRepository.findAllByUserId(TEST_USER_ID)).thenReturn(expectedBookings);

        
        List<Booking> result = bookingService.getAllBookingsByUserId(TEST_USER_ID);

        
        assertEquals(expectedBookings, result);
        verify(bookingRepository).findAllByUserId(TEST_USER_ID);
    }

    @Test
    void cancelBooking_shouldUpdateStatusAndSendEvent() {
        Booking booking = createTestBooking();
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        Booking result = bookingService.cancelBooking(TEST_BOOKING_ID);

        
        assertEquals(BookingStatus.CANCELLED, result.getStatus());
        verify(bookingRepository).findById(TEST_BOOKING_ID);
        verify(bookingRepository).save(booking);
        verify(bookingStatusEventProducer).sendEvent(booking);
    }

    @Test
    void createBooking_shouldCreateNewBookingWithPayment() {
        BookingCreateRequestModel request = new BookingCreateRequestModel(TEST_CAR_ID, FUTURE_DATE);
        CarDto carDto = CarDto.builder()
                .id(TEST_CAR_ID)
                .model("Tesla Model 3")
                .status(CarStatus.AVAILABLE)
                .creatorName("John Doe")
                .usdPerHour(BigDecimal.valueOf(50))
                .creatorId(TEST_CREATOR_ID)
                .build();

        PaymentDto paymentDto = PaymentDto.builder()
                .id(TEST_PAYMENT_ID)
                .bookingId(TEST_BOOKING_ID)
                .cardId(TEST_CARD_ID)
                .creatorId(TEST_CREATOR_ID)
                .usdTotalAmount(BigDecimal.valueOf(100))
                .status(PaymentStatus.PENDING)
                .build();

        when(currentUserService.getUserId()).thenReturn(TEST_USER_ID);
        when(carServiceClient.lockCarById(TEST_CAR_ID)).thenReturn(carDto);
        when(paymentServiceClient.createPayment(any(PaymentCreateModelDto.class))).thenReturn(paymentDto);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(TEST_BOOKING_ID);
            return b;
        });

        
        Booking result = bookingService.createBooking(request);

        
        assertNotNull(result);
        assertEquals(TEST_BOOKING_ID, result.getId());
        assertEquals(TEST_CAR_ID, result.getCarId());
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(BookingStatus.BOOKED, result.getStatus());
        assertEquals(TEST_PAYMENT_ID, result.getPaymentId());

        verify(carServiceClient).lockCarById(TEST_CAR_ID);
        verify(paymentServiceClient).createPayment(any(PaymentCreateModelDto.class));
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(bookingStatusEventProducer).sendEvent(any(Booking.class));
    }

    @Test
    void createBooking_shouldCalculateCorrectTotalAmount() {
        LocalDateTime endDate = LocalDateTime.now().plusHours(3).plusMinutes(15); // 3.25 hours
        BookingCreateRequestModel request = new BookingCreateRequestModel(TEST_CAR_ID, endDate);
        CarDto carDto = CarDto.builder()
                .id(TEST_CAR_ID)
                .model("Tesla Model 3")
                .status(CarStatus.AVAILABLE)
                .creatorName("John Doe")
                .usdPerHour(BigDecimal.valueOf(40)) // $40/hour
                .creatorId(TEST_CREATOR_ID)
                .build();
        BigDecimal expectedAmount = BigDecimal.valueOf(130); // 3.25 * 40 = 130

        PaymentDto paymentDto = PaymentDto.builder()
                .id(TEST_PAYMENT_ID)
                .bookingId(TEST_BOOKING_ID)
                .cardId(TEST_CARD_ID)
                .creatorId(TEST_CREATOR_ID)
                .usdTotalAmount(expectedAmount)
                .status(PaymentStatus.PENDING)
                .build();

        when(currentUserService.getUserId()).thenReturn(TEST_USER_ID);
        when(carServiceClient.lockCarById(TEST_CAR_ID)).thenReturn(carDto);
        when(paymentServiceClient.createPayment(any(PaymentCreateModelDto.class))).thenReturn(paymentDto);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        
        Booking result = bookingService.createBooking(request);

        
        assertEquals(0, expectedAmount.compareTo(result.getUsdTotalAmount()));
    }

    @Test
    void cancelExpiredBookings_shouldProcessPendingBookings() {
        List<Booking> pendingBookings = List.of(createTestBooking());
        when(bookingRepository.findByStatusAndCreatedAtBefore(
                eq(BookingStatus.BOOKED),
                any(LocalDateTime.class))
        ).thenReturn(pendingBookings);

        
        bookingService.cancelExpiredBookings();

        
        verify(bookingCancellationService).cancelExpiredBooking(pendingBookings.get(0));
        verify(bookingRepository).findByStatusAndCreatedAtBefore(
                eq(BookingStatus.BOOKED),
                any(LocalDateTime.class));
    }

    @Test
    void completeEndedRentals_shouldProcessCompletedBookings() {
        List<Booking> completedBookings = List.of(createTestBooking());
        when(bookingRepository.findByStatusAndEndDateBefore(
                eq(BookingStatus.RENTED),
                any(LocalDateTime.class))
        ).thenReturn(completedBookings);

        
        bookingService.completeEndedRentals();

        
        verify(bookingCompletionService).completeEndedRental(completedBookings.get(0));
        verify(bookingRepository).findByStatusAndEndDateBefore(
                eq(BookingStatus.RENTED),
                any(LocalDateTime.class));
    }

    private Booking createTestBooking() {
        return Booking.builder()
                .id(TEST_BOOKING_ID)
                .carId(TEST_CAR_ID)
                .userId(TEST_USER_ID)
                .usdTotalAmount(BigDecimal.valueOf(100))
                .startDate(TEST_DATE)
                .endDate(FUTURE_DATE)
                .createdAt(TEST_DATE)
                .status(BookingStatus.BOOKED)
                .paymentId(TEST_PAYMENT_ID)
                .build();
    }
}