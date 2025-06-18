package org.example.paymentservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.PaymentStatus;
import org.example.common.events.BookingStatusEvent;
import org.example.common.events.PaymentEvent;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.example.paymentservice.infrastructure.kafka.producers.PaymentEventProducer;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final CurrentUserService currentUserService;

    public List<Payment> findCurrentUsersPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findAllByCreatorIdAndStatus(currentUserService.getUserId(), status);
    }

    public Page<Payment> getCurrentUsersPaymentsPaged(@NonNull Pageable page) {
        return paymentRepository.getAllByCreatorId(currentUserService.getUserId(), page);
    }

    public Payment createPayment(@NonNull @Valid PaymentRequestCreateModel model) {
        Payment payment = Payment.builder()
                .carId(model.getCarId())
                .creatorId(currentUserService.getUserId())
                .bookingId(model.getBookingId())
                .usdTotalAmount(model.getUsdTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment created with id {}", savedPayment.getId());
        return savedPayment;
    }

    @Transactional
    public Payment performPayment(@NonNull UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + paymentId));

        if (payment.getStatus() == PaymentStatus.PAID){
            log.warn("Received payment request on paid payment with id {}", payment.getId());
            throw new BadRequestException("Payment is already paid");
        }
        if (payment.getStatus() == PaymentStatus.CANCELED){
            log.warn("Received payment request on cancelled payment with id {}", payment.getId());
            throw new BadRequestException("Cannot pay for cancelled payment");
        }

        //actual payment logic

        payment.setStatus(PaymentStatus.PAID);
        sendPaymentEvent(payment);

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment paid with id {}", savedPayment.getId());
        return savedPayment;
    }

    private void sendPaymentEvent(@NonNull @Valid Payment payment) {
        PaymentEvent event = PaymentEvent.builder()
                .paymentId(payment.getId())
                .bookingId(payment.getBookingId())
                .userId(payment.getCreatorId())
                .carId(payment.getCarId())
                .status(payment.getStatus())
                .timestamp(LocalDateTime.now())
                .build();
        paymentEventProducer.sendEvent(event);
    }
}