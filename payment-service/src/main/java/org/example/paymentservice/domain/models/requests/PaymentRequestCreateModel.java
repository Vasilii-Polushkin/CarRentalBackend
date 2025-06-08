package org.example.paymentservice.domain.models.requests;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestCreateModel {
    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID carId;

    @NotNull
    private BigDecimal amount;
}