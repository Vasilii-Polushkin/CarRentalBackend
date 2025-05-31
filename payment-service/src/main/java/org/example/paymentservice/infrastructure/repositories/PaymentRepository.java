package org.example.paymentservice.infrastructure.repositories;

import org.example.common.enums.PaymentStatus;
import org.example.paymentservice.domain.models.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByStatus(PaymentStatus status);
}