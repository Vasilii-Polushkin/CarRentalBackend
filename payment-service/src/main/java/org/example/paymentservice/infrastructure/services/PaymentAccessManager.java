package org.example.paymentservice.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.paymentservice.domain.models.entities.Payment;
import org.example.paymentservice.infrastructure.repositories.PaymentRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentAccessManager {
    private final PaymentRepository paymentRepository;
    private final CurrentUserService currentUserService;

    public boolean isOwner(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        return payment.getCreatorId().equals(currentUserService.getUserId());
    }
}