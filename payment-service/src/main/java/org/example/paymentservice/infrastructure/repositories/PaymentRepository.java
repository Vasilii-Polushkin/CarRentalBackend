package org.example.paymentservice.infrastructure.repositories;

import jakarta.validation.constraints.NotNull;
import org.example.common.enums.PaymentStatus;
import org.example.paymentservice.domain.models.entities.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, PagingAndSortingRepository<Payment, UUID> {
    List<Payment> findAllByCreatorIdAndStatus(@NotNull UUID creatorId, @NotNull PaymentStatus status);

    Page<Payment> getAllByCreatorId(@NotNull UUID creatorId, Pageable pageable);
}