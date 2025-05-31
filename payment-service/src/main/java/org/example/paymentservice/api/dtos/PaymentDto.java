package org.example.paymentservice.api.dtos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    @NotNull
    private UUID id;

    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID creatorId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private PaymentStatus status;
}