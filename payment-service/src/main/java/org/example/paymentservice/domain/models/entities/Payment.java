package org.example.paymentservice.domain.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.example.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID creatorId;

    @NotNull
    private UUID carId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdTotalAmount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentStatus status;
}