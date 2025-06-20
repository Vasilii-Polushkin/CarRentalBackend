package org.example.paymentservice.infrastructure.services;

import org.example.common.enums.PaymentStatus;
import org.example.common.events.PaymentEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.example.paymentservice.infrastructure.kafka.producers.PaymentEventProducer;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventProducer paymentEventProducer;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private PaymentService paymentService;

    private final UUID TEST_PAYMENT_ID = UUID.randomUUID();
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final UUID TEST_CAR_ID = UUID.randomUUID();
    private final UUID TEST_BOOKING_ID = UUID.randomUUID();
    private final BigDecimal TEST_AMOUNT = BigDecimal.valueOf(100.50);

    @Test
    void findPaymentById_shouldReturnPayment() {
        Payment expectedPayment = createTestPayment();
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(expectedPayment));

        Payment result = paymentService.findPaymentById(TEST_PAYMENT_ID);

        assertEquals(expectedPayment, result);
    }

    @Test
    void getPaymentsPagedByUserId_shouldReturnPayments() {
        Page<Payment> expectedPage = mock(Page.class);
        Pageable pageable = mock(Pageable.class);
        when(paymentRepository.getAllByCreatorId(TEST_USER_ID, pageable)).thenReturn(expectedPage);

        Page<Payment> result = paymentService.getPaymentsPagedByUserId(TEST_USER_ID, pageable);

        assertEquals(expectedPage, result);
    }

    @Test
    void findUserPaymentsByStatusAndCreatorId_shouldReturnFilteredPayments() {
        List<Payment> expectedPayments = List.of(createTestPayment());
        when(paymentRepository.findAllByCreatorIdAndStatus(TEST_USER_ID, PaymentStatus.PAID))
                .thenReturn(expectedPayments);

        List<Payment> result = paymentService.findUserPaymentsByStatusAndCreatorId(PaymentStatus.PAID, TEST_USER_ID);

        assertEquals(expectedPayments, result);
    }

    @Test
    void getAllPaymentsPaged_shouldReturnAllPayments() {
        Page<Payment> expectedPage = mock(Page.class);
        Pageable pageable = mock(Pageable.class);
        when(paymentRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Payment> result = paymentService.getAllPaymentsPaged(pageable);

        assertEquals(expectedPage, result);
    }

    @Test
    void createPayment_shouldCreateNewPayment() {
        PaymentRequestCreateModel model = new PaymentRequestCreateModel(TEST_CAR_ID, TEST_BOOKING_ID, TEST_AMOUNT);
        Payment payment = createTestPayment();

        when(currentUserService.getUserId()).thenReturn(TEST_USER_ID);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        Payment result = paymentService.createPayment(model);

        assertEquals(PaymentStatus.PENDING, result.getStatus());
        assertEquals(TEST_CAR_ID, result.getCarId());
        assertEquals(TEST_BOOKING_ID, result.getBookingId());
        assertEquals(TEST_AMOUNT, result.getUsdTotalAmount());
    }

    @Test
    void performPayment_shouldUpdatePaymentStatus() {
        Payment payment = createTestPayment();
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentService.performPayment(TEST_PAYMENT_ID);

        assertEquals(PaymentStatus.PAID, result.getStatus());
        verify(paymentEventProducer).sendEvent(any(PaymentEvent.class));
    }

    @Test
    void performPayment_shouldThrowWhenAlreadyPaid() {
        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> paymentService.performPayment(TEST_PAYMENT_ID));
    }

    @Test
    void performPayment_shouldThrowWhenCancelled() {
        Payment payment = createTestPayment();
        payment.setStatus(PaymentStatus.CANCELED);
        when(paymentRepository.findById(TEST_PAYMENT_ID)).thenReturn(Optional.of(payment));

        assertThrows(BadRequestException.class, () -> paymentService.performPayment(TEST_PAYMENT_ID));
    }

    private Payment createTestPayment() {
        return Payment.builder()
                .id(TEST_PAYMENT_ID)
                .carId(TEST_CAR_ID)
                .creatorId(TEST_USER_ID)
                .bookingId(TEST_BOOKING_ID)
                .usdTotalAmount(TEST_AMOUNT)
                .status(PaymentStatus.PENDING)
                .build();
    }
}