package org.example.paymentservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentEventProducer paymentEventProducer;
    private final CurrentUserService currentUserService;

    public List<Payment> findCurrentUsersPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findAllByCreatorIdAndStatus(currentUserService.getUserId(), status);
    }

    public Payment createPayment(PaymentRequestCreateModel model) {
        Payment payment = Payment.builder()
                .carId(model.getCarId())
                .creatorId(currentUserService.getUserId())
                .bookingId(model.getBookingId())
                .amount(model.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        return paymentRepository.save(payment);
    }

    public Payment cancelPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + paymentId));

        if (payment.getStatus() == PaymentStatus.PAID){
            throw new BadRequestException("Cannot cancel paid payment");
        }
        if (payment.getStatus() == PaymentStatus.CANCELED){
            throw new BadRequestException("Payment is already cancelled");
        }

        payment.setStatus(PaymentStatus.CANCELED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment performPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + paymentId));

        if (payment.getStatus() == PaymentStatus.PAID){
            throw new BadRequestException("Payment is already paid");
        }
        if (payment.getStatus() == PaymentStatus.CANCELED){
            throw new BadRequestException("Cannot pay for cancelled payment");
        }

        //actual payment logic

        payment.setStatus(PaymentStatus.PAID);
        sendPaymentEvent(payment);

        return paymentRepository.save(payment);
    }

    private void sendPaymentEvent(@Valid Payment payment) {
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

    public Page<Payment> getCurrentUsersPaymentsPaged(Pageable page) {
        return paymentRepository.getAllByCreatorId(currentUserService.getUserId(), page);
    }
}