package org.example.common.dtos;

import jakarta.validation.constraints.DecimalMin;
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
    private UUID cardId;

    @NotNull
    private UUID creatorId;

    @NotNull
    @DecimalMin("0")
    private BigDecimal usdTotalAmount;

    @NotNull
    private PaymentStatus status;
}