package org.example.common.events;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.common.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    @NotNull
    private UUID paymentId;

    @NotNull
    private UUID bookingId;

    @NotNull
    private UUID carId;

    @NotNull
    private UUID userId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private PaymentStatus status;

    @NotNull
    private LocalDateTime timestamp;
}