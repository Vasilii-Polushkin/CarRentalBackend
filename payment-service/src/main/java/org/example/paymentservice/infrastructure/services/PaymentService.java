package org.example.paymentservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.common.enums.PaymentStatus;
import org.example.common.exceptions.status_code_exceptions.BadRequestException;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.domain.models.requests.PaymentRequestCreateModel;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Payment createPayment(PaymentRequestCreateModel model) {
        Payment payment = Payment.builder()
                .creatorId(model.getPayerId())
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

        payment.setStatus(PaymentStatus.CANCELED);
        return paymentRepository.save(payment);
    }

    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + id));
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public List<Payment> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
}